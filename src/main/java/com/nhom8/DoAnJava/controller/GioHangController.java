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
import com.nhom8.DoAnJava.model.KhuyenMai;
import com.nhom8.DoAnJava.model.SanPham;
import com.nhom8.DoAnJava.model.TaiKhoan;
import com.nhom8.DoAnJava.repository.ChiTietHoaDonRepository;
import com.nhom8.DoAnJava.repository.GioHangRepository;
import com.nhom8.DoAnJava.repository.HoaDonRepository;
import com.nhom8.DoAnJava.repository.KhachHangRepository;
import com.nhom8.DoAnJava.repository.KhuyenMaiRepository;
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
    @Autowired private KhuyenMaiRepository khuyenMaiRepository;

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
        BigDecimal tongTien = tinhTongTien(lstGioHang);
        KetQuaKhuyenMai ketQuaKhuyenMai = tinhKhuyenMaiDangApDung(session, lstGioHang, tongTien);
        if (!ketQuaKhuyenMai.isHopLe()) {
            session.removeAttribute("MaKhuyenMai");
        }

        model.addAttribute("lstGioHang", lstGioHang);
        model.addAttribute("TongSoLuong", tongSoLuong);
        model.addAttribute("TongTien", tongTien);
        model.addAttribute("MaKhuyenMai", ketQuaKhuyenMai.getMaKM());
        model.addAttribute("TenKhuyenMai", ketQuaKhuyenMai.getTenKM());
        model.addAttribute("TienGiam", ketQuaKhuyenMai.getTienGiam());
        model.addAttribute("TongTienSauGiam", ketQuaKhuyenMai.getTongTienSauGiam());
        model.addAttribute("ThongBaoKhuyenMai", ketQuaKhuyenMai.getThongBao());
        return "giohang/GioHang";
    }

    @PostMapping("/ap-dung-khuyen-mai")
    public String apDungKhuyenMai(
            @RequestParam(value = "maKhuyenMai", required = false) String maKhuyenMai,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        List<ItemGioHangDTO> lstGioHang = layGioHang(session);
        if (lstGioHang.isEmpty()) {
            redirectAttributes.addFlashAttribute("Error", "Gio hang dang trong.");
            return "redirect:/gio-hang/";
        }

        String maKM = chuanHoaMaKhuyenMai(maKhuyenMai);
        if (maKM == null) {
            session.removeAttribute("MaKhuyenMai");
            redirectAttributes.addFlashAttribute("Error", "Vui long nhap ma khuyen mai.");
            return "redirect:/gio-hang/";
        }

        BigDecimal tongTien = tinhTongTien(lstGioHang);
        KetQuaKhuyenMai ketQuaKhuyenMai = tinhKhuyenMai(maKM, lstGioHang, tongTien);
        if (!ketQuaKhuyenMai.isHopLe()) {
            session.removeAttribute("MaKhuyenMai");
            redirectAttributes.addFlashAttribute("Error", ketQuaKhuyenMai.getThongBao());
            return "redirect:/gio-hang/";
        }

        session.setAttribute("MaKhuyenMai", ketQuaKhuyenMai.getMaKM());
        redirectAttributes.addFlashAttribute("Success", "Da ap dung ma khuyen mai " + ketQuaKhuyenMai.getMaKM() + ".");
        return "redirect:/gio-hang/";
    }

    @PostMapping("/huy-khuyen-mai")
    public String huyKhuyenMai(HttpSession session, RedirectAttributes redirectAttributes) {
        session.removeAttribute("MaKhuyenMai");
        redirectAttributes.addFlashAttribute("Success", "Da huy ma khuyen mai.");
        return "redirect:/gio-hang/";
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

            KetQuaKhuyenMai ketQuaKhuyenMai = tinhKhuyenMaiDangApDung(session, lstGioHang, tongTien);
            if (!ketQuaKhuyenMai.isHopLe()) {
                session.removeAttribute("MaKhuyenMai");
                throw new RuntimeException(ketQuaKhuyenMai.getThongBao());
            }
            BigDecimal tongTienSauGiam = ketQuaKhuyenMai.getTongTienSauGiam();

            String maHD = generateMaHD();
            HoaDon hoaDon = new HoaDon();
            hoaDon.setMaHD(maHD);
            hoaDon.setKhachHang(khachHang);
            hoaDon.setNgayLap(LocalDateTime.now());
            hoaDon.setTongTienHD(tongTienSauGiam);
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

            if (ketQuaKhuyenMai.getMaKM() != null) {
                khuyenMaiRepository.findByMaKMIgnoreCase(ketQuaKhuyenMai.getMaKM()).ifPresent(km -> {
                    km.setMaHD(maHD);
                    khuyenMaiRepository.save(km);
                });
            }

            gioHangRepository.deleteAll(gioHangRepository.findByMaTK(maTK));
            session.removeAttribute("GioHang");
            session.removeAttribute("MaKhuyenMai");
            
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

    private BigDecimal tinhTongTien(List<ItemGioHangDTO> lstGioHang) {
        BigDecimal tongTien = BigDecimal.ZERO;
        for (ItemGioHangDTO item : lstGioHang) {
            if (item == null) {
                continue;
            }
            BigDecimal donGia = BigDecimal.valueOf(item.getdDonGia() != null ? item.getdDonGia() : 0);
            tongTien = tongTien.add(donGia.multiply(BigDecimal.valueOf(laySoLuongItem(item))));
        }
        return tongTien;
    }

    private KetQuaKhuyenMai tinhKhuyenMaiDangApDung(HttpSession session, List<ItemGioHangDTO> lstGioHang, BigDecimal tongTien) {
        String maKM = chuanHoaMaKhuyenMai((String) session.getAttribute("MaKhuyenMai"));
        if (maKM == null) {
            return KetQuaKhuyenMai.khongApDung(tongTien);
        }
        return tinhKhuyenMai(maKM, lstGioHang, tongTien);
    }

    private KetQuaKhuyenMai tinhKhuyenMai(String maKM, List<ItemGioHangDTO> lstGioHang, BigDecimal tongTien) {
        KhuyenMai khuyenMai = khuyenMaiRepository.findByMaKMIgnoreCase(maKM).orElse(null);
        if (khuyenMai == null) {
            return KetQuaKhuyenMai.khongHopLe(maKM, tongTien, "Ma khuyen mai khong ton tai.");
        }

        LocalDateTime now = LocalDateTime.now();
        if (khuyenMai.getNgayBD() != null && khuyenMai.getNgayBD().isAfter(now)) {
            return KetQuaKhuyenMai.khongHopLe(khuyenMai.getMaKM(), tongTien, "Ma khuyen mai chua den ngay ap dung.");
        }
        if (khuyenMai.getNgayKT() != null && khuyenMai.getNgayKT().isBefore(now)) {
            return KetQuaKhuyenMai.khongHopLe(khuyenMai.getMaKM(), tongTien, "Ma khuyen mai da het han.");
        }
        if (khuyenMai.getSoTienToiThieuNhanKM() != null && tongTien.compareTo(khuyenMai.getSoTienToiThieuNhanKM()) < 0) {
            return KetQuaKhuyenMai.khongHopLe(khuyenMai.getMaKM(), tongTien, "Don hang chua dat gia tri toi thieu de nhan khuyen mai.");
        }

        BigDecimal tienApDung = tinhTienApDungKhuyenMai(khuyenMai.getMaKM(), lstGioHang);
        if (tienApDung.compareTo(BigDecimal.ZERO) <= 0) {
            return KetQuaKhuyenMai.khongHopLe(khuyenMai.getMaKM(), tongTien, "Ma khuyen mai khong ap dung cho san pham trong gio hang.");
        }

        BigDecimal phanTram = parsePhanTramKhuyenMai(khuyenMai.getPhanTramKM());
        BigDecimal tienGiam = tienApDung.multiply(phanTram).divide(BigDecimal.valueOf(100));
        if (khuyenMai.getSoTienToiDaKM() != null && khuyenMai.getSoTienToiDaKM().compareTo(BigDecimal.ZERO) > 0) {
            tienGiam = tienGiam.min(khuyenMai.getSoTienToiDaKM());
        }
        if (tienGiam.compareTo(tongTien) > 0) {
            tienGiam = tongTien;
        }

        return KetQuaKhuyenMai.hopLe(
                khuyenMai.getMaKM(),
                khuyenMai.getTenKM(),
                tienGiam,
                tongTien.subtract(tienGiam));
    }

    private BigDecimal tinhTienApDungKhuyenMai(String maKM, List<ItemGioHangDTO> lstGioHang) {
        if (khuyenMaiRepository.countChiTietByMaKM(maKM) <= 0) {
            return tinhTongTien(lstGioHang);
        }

        List<String> maSPApDung = khuyenMaiRepository.findMaSPApDungByMaKM(maKM);
        BigDecimal tongTienApDung = BigDecimal.ZERO;
        for (ItemGioHangDTO item : lstGioHang) {
            if (item != null && maSPApDung.contains(item.getiMaSP())) {
                BigDecimal donGia = BigDecimal.valueOf(item.getdDonGia() != null ? item.getdDonGia() : 0);
                tongTienApDung = tongTienApDung.add(donGia.multiply(BigDecimal.valueOf(laySoLuongItem(item))));
            }
        }
        return tongTienApDung;
    }

    private BigDecimal parsePhanTramKhuyenMai(String phanTramKM) {
        if (phanTramKM == null || phanTramKM.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }

        String value = phanTramKM.trim()
                .replace("%", "")
                .replace(",", ".")
                .replaceAll("[^0-9.]", "");

        if (value.isEmpty()) {
            return BigDecimal.ZERO;
        }

        try {
            return new BigDecimal(value);
        } catch (NumberFormatException ex) {
            return BigDecimal.ZERO;
        }
    }

    private String chuanHoaMaKhuyenMai(String maKhuyenMai) {
        if (maKhuyenMai == null || maKhuyenMai.trim().isEmpty()) {
            return null;
        }
        return maKhuyenMai.trim().toUpperCase();
    }

    private static class KetQuaKhuyenMai {
        private final boolean hopLe;
        private final String maKM;
        private final String tenKM;
        private final BigDecimal tienGiam;
        private final BigDecimal tongTienSauGiam;
        private final String thongBao;

        private KetQuaKhuyenMai(boolean hopLe, String maKM, String tenKM, BigDecimal tienGiam, BigDecimal tongTienSauGiam, String thongBao) {
            this.hopLe = hopLe;
            this.maKM = maKM;
            this.tenKM = tenKM;
            this.tienGiam = tienGiam;
            this.tongTienSauGiam = tongTienSauGiam;
            this.thongBao = thongBao;
        }

        static KetQuaKhuyenMai khongApDung(BigDecimal tongTien) {
            return new KetQuaKhuyenMai(true, null, null, BigDecimal.ZERO, tongTien, null);
        }

        static KetQuaKhuyenMai khongHopLe(String maKM, BigDecimal tongTien, String thongBao) {
            return new KetQuaKhuyenMai(false, maKM, null, BigDecimal.ZERO, tongTien, thongBao);
        }

        static KetQuaKhuyenMai hopLe(String maKM, String tenKM, BigDecimal tienGiam, BigDecimal tongTienSauGiam) {
            return new KetQuaKhuyenMai(true, maKM, tenKM, tienGiam, tongTienSauGiam.max(BigDecimal.ZERO), null);
        }

        boolean isHopLe() {
            return hopLe;
        }

        String getMaKM() {
            return maKM;
        }

        String getTenKM() {
            return tenKM;
        }

        BigDecimal getTienGiam() {
            return tienGiam;
        }

        BigDecimal getTongTienSauGiam() {
            return tongTienSauGiam;
        }

        String getThongBao() {
            return thongBao;
        }
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
