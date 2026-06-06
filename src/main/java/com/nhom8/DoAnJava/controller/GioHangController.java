package com.nhom8.DoAnJava.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
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

    @GetMapping("/them")
    public String themGioHang(@RequestParam("iMaSP") String iMaSP, HttpSession session) {
        List<ItemGioHangDTO> lstGioHang = layGioHang(session);
        SanPham sp = sanPhamRepository.findById(iMaSP).orElse(null);
        if (sp == null) return "redirect:/trang-chu";

        ItemGioHangDTO item = lstGioHang.stream().filter(n -> n.getiMaSP().equals(iMaSP)).findFirst().orElse(null);

        if (item == null) {
            item = new ItemGioHangDTO(sp.getMaSP(), sp.getTenSP(), "no-image.jpg", sp.getDonGiaSP().doubleValue());
            lstGioHang.add(item);
        } else {
            if (item.getiSoLuong() < 10) item.setiSoLuong(item.getiSoLuong() + 1);
        }

        String maTK = (String) session.getAttribute("UserID");
        if (maTK != null) {
            GioHang ghDB = gioHangRepository.findByMaTKAndMaSP(maTK, iMaSP);
            if (ghDB == null) {
                GioHang moi = new GioHang();
                moi.setMaTK(maTK);
                moi.setMaSP(iMaSP);
                moi.setSoLuong(item.getiSoLuong());
                gioHangRepository.save(moi);
            } else {
                ghDB.setSoLuong(item.getiSoLuong());
                gioHangRepository.save(ghDB);
            }
        }
        session.setAttribute("GioHang", lstGioHang);
        return "redirect:/trang-chu";
    }

    @GetMapping({"", "/"})
    public String xemGioHang(HttpSession session, Model model) {
        List<ItemGioHangDTO> lstGioHang = layGioHang(session);
        int tongSoLuong = lstGioHang.stream().mapToInt(ItemGioHangDTO::getiSoLuong).sum();
        double tongTien = lstGioHang.stream().mapToDouble(ItemGioHangDTO::getDThanhTien).sum();
        model.addAttribute("lstGioHang", lstGioHang);
        model.addAttribute("TongSoLuong", tongSoLuong);
        model.addAttribute("TongTien", tongTien);
        return "giohang/GioHang";
    }

    @PostMapping("/cap-nhat")
    public String capNhatGioHang(@RequestParam("iMaSP") String iMaSP, @RequestParam("txtSoLuong") int txtSoLuong, HttpSession session) {
        if (txtSoLuong < 1) txtSoLuong = 1;
        if (txtSoLuong > 10) txtSoLuong = 10;

        List<ItemGioHangDTO> lstGioHang = layGioHang(session);
        ItemGioHangDTO item = lstGioHang.stream().filter(n -> n.getiMaSP().equals(iMaSP)).findFirst().orElse(null);

        if (item != null) {
            item.setiSoLuong(txtSoLuong);
            String maTK = (String) session.getAttribute("UserID");
            if (maTK != null) {
                GioHang ghDB = gioHangRepository.findByMaTKAndMaSP(maTK, iMaSP);
                if (ghDB != null) {
                    ghDB.setSoLuong(txtSoLuong);
                    gioHangRepository.save(ghDB);
                }
            }
        }
        return "redirect:/gio-hang/";
    }

    @GetMapping("/xoa")
    public String xoaGioHang(@RequestParam("iMaSP") String iMaSP, HttpSession session) {
        List<ItemGioHangDTO> lstGioHang = layGioHang(session);
        ItemGioHangDTO item = lstGioHang.stream().filter(n -> n.getiMaSP().equals(iMaSP)).findFirst().orElse(null);

        if (item != null) {
            lstGioHang.remove(item);
            String maTK = (String) session.getAttribute("UserID");
            if (maTK != null) {
                GioHang ghDB = gioHangRepository.findByMaTKAndMaSP(maTK, iMaSP);
                if (ghDB != null) gioHangRepository.delete(ghDB);
            }
        }
        return "redirect:/gio-hang/";
    }

    @GetMapping("/dat-hang")
    @Transactional
    public String datHang(HttpSession session, RedirectAttributes redirectAttributes) {
        String maTK = (String) session.getAttribute("UserID");
        if (maTK == null) return "redirect:/tai-khoan/dang-nhap";

        TaiKhoan taiKhoan = taiKhoanRepository.findById(maTK).orElse(null);
        KhachHang khachHang = (taiKhoan != null && taiKhoan.getMaKH() != null) 
                                ? khachHangRepository.findById(taiKhoan.getMaKH()).orElse(null) : null;

        if (khachHang == null || khachHang.getTenKH() == null || khachHang.getDiaChiKH() == null || khachHang.getSdtKH() == null) {
            redirectAttributes.addFlashAttribute("ThongBao", "Vui lòng cập nhật đầy đủ thông tin (Tên, Địa chỉ, SĐT) để giao hàng.");
            return "redirect:/tai-khoan/thong-tin-tai-khoan";
        }

        List<ItemGioHangDTO> lstGioHang = layGioHang(session);
        if (lstGioHang.isEmpty()) return "redirect:/trang-chu";

        try {
            HoaDon ddh = new HoaDon();
            ddh.setMaHD(generateMaHD());
            ddh.setKhachHang(khachHang);
            ddh.setNgayLap(LocalDateTime.now());
            ddh.setTrangThaiTT("Đã đặt");
            double tongTien = lstGioHang.stream().mapToDouble(ItemGioHangDTO::getDThanhTien).sum();
            ddh.setTongTienHD(BigDecimal.valueOf(tongTien));

            hoaDonRepository.save(ddh);

            for (ItemGioHangDTO item : lstGioHang) {
                ChiTietHoaDon cthd = new ChiTietHoaDon();
                cthd.setMaHD(ddh.getMaHD());
                cthd.setMaSP(item.getiMaSP());
                cthd.setSoLuongSP_HD(item.getiSoLuong().toString());
                cthd.setThanhTien(BigDecimal.valueOf(item.getDThanhTien()));
                chiTietHoaDonRepository.save(cthd);

                SanPham sp = sanPhamRepository.findById(item.getiMaSP()).orElse(null);
                if (sp != null) {
                    if (sp.getSoLuongTon() < item.getiSoLuong()) throw new RuntimeException("Sản phẩm không đủ tồn kho.");
                    sp.setSoLuongTon(sp.getSoLuongTon() - item.getiSoLuong());
                    sanPhamRepository.save(sp);
                }
            }

            List<GioHang> cartInDb = gioHangRepository.findByMaTK(maTK);
            gioHangRepository.deleteAll(cartInDb);
            session.removeAttribute("GioHang");

            return "redirect:/gio-hang/dat-hang-thanh-cong";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("Error", "Lỗi đặt hàng: " + ex.getMessage());
            return "redirect:/gio-hang/";
        }
    }

    @GetMapping("/dat-hang-thanh-cong")
    public String datHangThanhCong() {
        return "giohang/DatHangThanhCong";
    }

    private String generateMaHD() {
        HoaDon lastHD = hoaDonRepository.findFirstByOrderByMaHDDesc();
        if (lastHD == null) return "HD00001";
        try {
            int nextSo = Integer.parseInt(lastHD.getMaHD().substring(2)) + 1;
            System.out.println("Generated MaHD: " + String.format("HD%05d", nextSo));
            while (hoaDonRepository.existsById(String.format("HD%05d", nextSo))) {
                nextSo++;
            }
            return String.format("HD%05d", nextSo);
        } catch (Exception e) {
            return "HD" + System.currentTimeMillis();
        }
    }
}