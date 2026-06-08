package com.nhom8.DoAnJava.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.nhom8.DoAnJava.dto.ItemGioHangDTO;
import com.nhom8.DoAnJava.model.ChiTietHoaDon;
import com.nhom8.DoAnJava.model.GioHang;
import com.nhom8.DoAnJava.model.HoaDon;
import com.nhom8.DoAnJava.model.KhachHang;
import com.nhom8.DoAnJava.model.SanPham;
import com.nhom8.DoAnJava.model.TaiKhoan;
import com.nhom8.DoAnJava.repository.ChiTietHoaDonRepository;
import com.nhom8.DoAnJava.repository.GioHangRepository;
import com.nhom8.DoAnJava.repository.HoaDonRepository;
import com.nhom8.DoAnJava.repository.KhachHangRepository;
import com.nhom8.DoAnJava.repository.SanPhamRepository;
import com.nhom8.DoAnJava.repository.TaiKhoanRepository;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

@Controller
@RequestMapping("/gio-hang")
public class GioHangController {

    @Autowired private SanPhamRepository sanPhamRepository;
    @Autowired private GioHangRepository gioHangRepository;
    @Autowired private TaiKhoanRepository taiKhoanRepository;
    @Autowired private KhachHangRepository khachHangRepository;
    @Autowired private HoaDonRepository hoaDonRepository;
    @Autowired private ChiTietHoaDonRepository chiTietHoaDonRepository;

    private List<ItemGioHangDTO> layGioHang(HttpSession session) {
        @SuppressWarnings("unchecked")
        List<ItemGioHangDTO> lstGioHang = (List<ItemGioHangDTO>) session.getAttribute("GioHang");
        if (lstGioHang == null) {
            lstGioHang = new ArrayList<>();
            session.setAttribute("GioHang", lstGioHang);
        }
        return lstGioHang;
    }

    private ItemGioHangDTO timItemTrongGio(List<ItemGioHangDTO> lstGioHang, String maSP) {
        return lstGioHang.stream().filter(item -> item.getiMaSP().equals(maSP)).findFirst().orElse(null);
    }

    private int laySoLuongItem(ItemGioHangDTO item) {
        try { return item.getiSoLuong(); } catch (Exception e) { return 0; }
    }

    private int layTonKho(SanPham sp) {
        return (sp == null || sp.getSoLuongTon() == null) ? 0 : sp.getSoLuongTon();
    }

    private String layAnhDauTien(SanPham sp) {
        if (sp != null && sp.getDanhSachAnhs() != null && !sp.getDanhSachAnhs().isEmpty()) {
            return sp.getDanhSachAnhs().get(0).getTenAnh();
        }
        return "no-image.jpg";
    }

    private void dongBoGioHangDB(String maTK, ItemGioHangDTO item) {
        if (maTK == null || item == null) return;
        GioHang ghDB = gioHangRepository.findByMaTKAndMaSP(maTK, item.getiMaSP());
        if (ghDB == null) {
            ghDB = new GioHang();
            ghDB.setMaTK(maTK);
            ghDB.setMaSP(item.getiMaSP());
            ghDB.setNgayThem(LocalDateTime.now());
        }
        ghDB.setSoLuong(laySoLuongItem(item));
        gioHangRepository.save(ghDB);
    }

    @GetMapping("/them")
    public String themGioHang(@RequestParam("iMaSP") String iMaSP, HttpSession session, RedirectAttributes redirectAttributes) {
        SanPham sp = sanPhamRepository.findById(iMaSP).orElse(null);
        if (sp == null) {
            redirectAttributes.addFlashAttribute("Error", "Sản phẩm không tồn tại.");
            return "redirect:/trang-chu";
        }

        int tonKho = layTonKho(sp);
        if (tonKho <= 0) {
            redirectAttributes.addFlashAttribute("Error", "Sản phẩm '" + sp.getTenSP() + "' hiện đã hết hàng.");
            return "redirect:/trang-chu";
        }

        List<ItemGioHangDTO> lstGioHang = layGioHang(session);
        ItemGioHangDTO item = timItemTrongGio(lstGioHang, iMaSP);
        int soLuongToiDa = tonKho; // ĐỔI THÀNH TONKHO (Bỏ giới hạn số 10)

        if (item == null) {
            BigDecimal donGia = sp.getDonGiaSP() != null ? sp.getDonGiaSP() : BigDecimal.ZERO;
            item = new ItemGioHangDTO(sp.getMaSP(), sp.getTenSP(), layAnhDauTien(sp), donGia.doubleValue());
            item.setiSoLuong(1);
            lstGioHang.add(item);
        } else {
            int soLuongHienTai = laySoLuongItem(item);
            if (soLuongHienTai >= soLuongToiDa) {
                redirectAttributes.addFlashAttribute("Error", "Sản phẩm chỉ còn tối đa " + soLuongToiDa + " sản phẩm.");
                return "redirect:/gio-hang/";
            }
            item.setiSoLuong(soLuongHienTai + 1);
        }

        dongBoGioHangDB((String) session.getAttribute("UserID"), item);
        session.setAttribute("GioHang", lstGioHang);
        redirectAttributes.addFlashAttribute("Success", "Đã thêm sản phẩm vào giỏ hàng.");
        return "redirect:/gio-hang/";
    }

    @GetMapping({"", "/"})
    public String xemGioHang(HttpSession session, Model model) {
        List<ItemGioHangDTO> lstGioHang = layGioHang(session);
        
        // BỔ SUNG: Tạo một Map để lấy đúng số lượng tồn kho thực tế của từng SP trong giỏ
        Map<String, Integer> mapTonKho = new HashMap<>();
        for (ItemGioHangDTO item : lstGioHang) {
            SanPham sp = sanPhamRepository.findById(item.getiMaSP()).orElse(null);
            mapTonKho.put(item.getiMaSP(), sp != null && sp.getSoLuongTon() != null ? sp.getSoLuongTon() : 0);
        }
        model.addAttribute("mapTonKho", mapTonKho); // Đẩy map này ra ngoài giao diện HTML

        int tongSoLuong = lstGioHang.stream().mapToInt(this::laySoLuongItem).sum();
        double tongTien = lstGioHang.stream().mapToDouble(ItemGioHangDTO::getDThanhTien).sum();

        model.addAttribute("lstGioHang", lstGioHang);
        model.addAttribute("TongSoLuong", tongSoLuong);
        model.addAttribute("TongTien", tongTien);
        return "giohang/GioHang";
    }

    @PostMapping("/cap-nhat")
    public String capNhatGioHang(@RequestParam("iMaSP") String iMaSP, @RequestParam("txtSoLuong") int txtSoLuong, HttpSession session, RedirectAttributes redirectAttributes) {
        SanPham sp = sanPhamRepository.findById(iMaSP).orElse(null);
        if (sp == null) {
            redirectAttributes.addFlashAttribute("Error", "Sản phẩm không tồn tại.");
            return "redirect:/gio-hang/";
        }

        int tonKho = layTonKho(sp);
        if (tonKho <= 0) {
            xoaSanPhamKhoiSessionVaDB(iMaSP, session);
            redirectAttributes.addFlashAttribute("Error", "Sản phẩm đã hết hàng và được xóa khỏi giỏ.");
            return "redirect:/gio-hang/";
        }

        if (txtSoLuong < 1) txtSoLuong = 1;
        int soLuongToiDa = tonKho; // ĐỔI THÀNH TONKHO (Bỏ giới hạn số 10)

        if (txtSoLuong > soLuongToiDa) {
            txtSoLuong = soLuongToiDa;
            redirectAttributes.addFlashAttribute("Error", "Số lượng tự động điều chỉnh. Sản phẩm '" + sp.getTenSP() + "' chỉ còn tối đa " + soLuongToiDa + " sản phẩm.");
        }

        List<ItemGioHangDTO> lstGioHang = layGioHang(session);
        ItemGioHangDTO item = timItemTrongGio(lstGioHang, iMaSP);
        if (item != null) {
            item.setiSoLuong(txtSoLuong);
            dongBoGioHangDB((String) session.getAttribute("UserID"), item);
        }

        session.setAttribute("GioHang", lstGioHang);
        return "redirect:/gio-hang/";
    }
    @GetMapping("/xoa")
    public String xoaGioHang(@RequestParam("iMaSP") String iMaSP, HttpSession session, RedirectAttributes redirectAttributes) {
        xoaSanPhamKhoiSessionVaDB(iMaSP, session);
        redirectAttributes.addFlashAttribute("Success", "Đã xóa sản phẩm khỏi giỏ hàng.");
        return "redirect:/gio-hang/";
    }

    private void xoaSanPhamKhoiSessionVaDB(String maSP, HttpSession session) {
        List<ItemGioHangDTO> lstGioHang = layGioHang(session);
        ItemGioHangDTO item = timItemTrongGio(lstGioHang, maSP);
        if (item != null) lstGioHang.remove(item);

        String maTK = (String) session.getAttribute("UserID");
        if (maTK != null) {
            GioHang ghDB = gioHangRepository.findByMaTKAndMaSP(maTK, maSP);
            if (ghDB != null) gioHangRepository.delete(ghDB);
        }
        session.setAttribute("GioHang", lstGioHang);
    }

    @GetMapping("/dat-hang")
    @Transactional
    public String datHang(@RequestParam(value = "phuongThucTT", defaultValue = "COD") String phuongThucTT, HttpSession session, RedirectAttributes redirectAttributes) {
        String maTK = (String) session.getAttribute("UserID");
        if (maTK == null) return "redirect:/tai-khoan/dang-nhap";

        TaiKhoan taiKhoan = taiKhoanRepository.findById(maTK).orElse(null);
        KhachHang khachHang = (taiKhoan != null && taiKhoan.getMaKH() != null) ? khachHangRepository.findById(taiKhoan.getMaKH()).orElse(null) : null;

        if (khachHang == null || khachHang.getTenKH() == null || khachHang.getTenKH().trim().isEmpty()
                || khachHang.getDiaChiKH() == null || khachHang.getDiaChiKH().trim().isEmpty()
                || khachHang.getSdtKH() == null || khachHang.getSdtKH().trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("ThongBao", "Vui lòng cập nhật đầy đủ thông tin Tên, Địa chỉ, SĐT để giao hàng.");
            return "redirect:/tai-khoan/thong-tin-tai-khoan";
        }

        List<ItemGioHangDTO> lstGioHang = layGioHang(session);
        if (lstGioHang.isEmpty()) {
            redirectAttributes.addFlashAttribute("Error", "Giỏ hàng đang trống.");
            return "redirect:/trang-chu";
        }

        try {
            Map<String, SanPham> mapSanPham = new HashMap<>();
            Map<String, Integer> mapSoLuong = new HashMap<>();
            Map<String, BigDecimal> mapThanhTien = new HashMap<>();
            BigDecimal tongTien = BigDecimal.ZERO;

            for (ItemGioHangDTO item : lstGioHang) {
                if (item == null || item.getiMaSP() == null) throw new RuntimeException("Giỏ hàng không hợp lệ.");
                SanPham sp = sanPhamRepository.findById(item.getiMaSP()).orElse(null);
                if (sp == null) throw new RuntimeException("Sản phẩm không tồn tại.");

                int soLuongMua = laySoLuongItem(item);
                int tonKho = layTonKho(sp);
                if (tonKho <= 0 || tonKho < soLuongMua) throw new RuntimeException("Sản phẩm '" + sp.getTenSP() + "' không đủ hàng.");

                BigDecimal donGia = sp.getDonGiaSP() != null ? sp.getDonGiaSP() : BigDecimal.ZERO;
                BigDecimal thanhTien = donGia.multiply(BigDecimal.valueOf(soLuongMua));

                mapSanPham.put(item.getiMaSP(), sp);
                mapSoLuong.put(item.getiMaSP(), soLuongMua);
                mapThanhTien.put(item.getiMaSP(), thanhTien);
                tongTien = tongTien.add(thanhTien);
            }

            String maHD = generateMaHD();
            HoaDon hoaDon = new HoaDon();
            hoaDon.setMaHD(maHD);
            hoaDon.setKhachHang(khachHang);
            hoaDon.setNgayLap(LocalDateTime.now());
            hoaDon.setTongTienHD(tongTien);
            hoaDon.setTrangThaiTT(("QR".equals(phuongThucTT) || "CASH".equals(phuongThucTT)) ? "Đã thanh toán" : "Đã đặt");
            hoaDonRepository.save(hoaDon);

            for (ItemGioHangDTO item : lstGioHang) {
                String maSP = item.getiMaSP();
                ChiTietHoaDon chiTiet = new ChiTietHoaDon();
                chiTiet.setMaHD(maHD);
                chiTiet.setMaSP(maSP);
                chiTiet.setSoLuongSP_HD(String.valueOf(mapSoLuong.get(maSP)));
                chiTiet.setThanhTien(mapThanhTien.get(maSP));
                chiTietHoaDonRepository.save(chiTiet);

                SanPham sp = mapSanPham.get(maSP);
                sp.setSoLuongTon(layTonKho(sp) - mapSoLuong.get(maSP));
                sanPhamRepository.save(sp);
            }

            gioHangRepository.deleteAll(gioHangRepository.findByMaTK(maTK));
            session.removeAttribute("GioHang");
            
            return "redirect:/gio-hang/dat-hang-thanh-cong?id=" + maHD + "&pttt=" + phuongThucTT;

        } catch (Exception ex) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            redirectAttributes.addFlashAttribute("Error", "Lỗi đặt hàng: " + ex.getMessage());
            return "redirect:/gio-hang/";
        }
    }

    @GetMapping("/dat-hang-thanh-cong")
    public String datHangThanhCong(
            @RequestParam(value = "id", required = false) String id,
            @RequestParam(value = "pttt", required = false) String pttt,
            Model model) {
        if (id != null) {
            hoaDonRepository.findById(id).ifPresent(hd -> {
                model.addAttribute("MaHD", hd.getMaHD());
                model.addAttribute("TongTien", hd.getTongTienHD());
                model.addAttribute("PhuongThucTT", pttt);
            });
        }
        return "giohang/DatHangThanhCong";
    }

    private synchronized String generateMaHD() {
        HoaDon lastHD = hoaDonRepository.findFirstByOrderByMaHDDesc();
        if (lastHD == null || lastHD.getMaHD() == null) return "HD00001";
        try {
            int nextSo = Integer.parseInt(lastHD.getMaHD().substring(2)) + 1;
            while (hoaDonRepository.existsById(String.format("HD%05d", nextSo))) { nextSo++; }
            return String.format("HD%05d", nextSo);
        } catch (Exception e) {
            return "HD_ERR_" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
    }
}