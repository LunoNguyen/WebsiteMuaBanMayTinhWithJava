package com.nhom8.DoAnJava.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.DigestUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.nhom8.DoAnJava.dto.DangKyDTO;
import com.nhom8.DoAnJava.dto.DangNhapDTO;
import com.nhom8.DoAnJava.model.ChiTietHoaDon;
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
import jakarta.validation.Valid;

@Controller
@RequestMapping("/tai-khoan")
public class TaiKhoanController {

    @Autowired private TaiKhoanRepository taiKhoanRepository;
    @Autowired private KhachHangRepository khachHangRepository;
    @Autowired private GioHangRepository gioHangRepository;
    @Autowired private HoaDonRepository hoaDonRepository;
    @Autowired private ChiTietHoaDonRepository chiTietHoaDonRepository;
    @Autowired private SanPhamRepository sanPhamRepository;

    @GetMapping("/dang-nhap")
    public String dangNhap(Model model) {
        model.addAttribute("dangNhapDTO", new DangNhapDTO());
        return "taikhoan/DangNhap";
    }

    @PostMapping("/xu-ly-dang-nhap")
    public String xuLyDangNhap(@Valid @ModelAttribute DangNhapDTO dto, BindingResult result, HttpSession session, Model model) {
        if (result.hasErrors()) return "taikhoan/DangNhap";
        
        String hashedPass = DigestUtils.md5DigestAsHex(dto.getPassword().getBytes()).toUpperCase();
        TaiKhoan tk = taiKhoanRepository.findByEmailTKAndMatKhau(dto.getEmail(), hashedPass).orElse(null);
        
        if (tk == null) {
            model.addAttribute("error", "Email hoặc Mật khẩu không chính xác.");
            return "taikhoan/DangNhap";
        }
        
        session.setAttribute("UserID", tk.getMaTK());
        session.setAttribute("UserEmail", tk.getEmailTK());
        session.setAttribute("AccountType", tk.getLoaiTaiKhoan());
        
        if ("Admin".equals(tk.getLoaiTaiKhoan()) || "Nhân viên".equals(tk.getLoaiTaiKhoan())) {
            return "redirect:/admin/trang-admin";
        }
        return "redirect:/trang-chu";
    }

    @GetMapping("/dang-ky")
    public String dangKy(Model model) {
        model.addAttribute("dangKyDTO", new DangKyDTO());
        return "taikhoan/DangKy";
    }

    @PostMapping("/xu-ly-dang-ky")
    @Transactional
    public String xuLyDangKy(@Valid @ModelAttribute DangKyDTO dto, BindingResult result, Model model) {
        if (result.hasErrors()) return "taikhoan/DangKy";
        if (taiKhoanRepository.findByEmailTK(dto.getEmail()).isPresent()) {
            model.addAttribute("error", "Email này đã được sử dụng.");
            return "taikhoan/DangKy";
        }

        KhachHang kh = new KhachHang();
        kh.setMaKH(generateMaKH());
        kh.setEmailKH(dto.getEmail());
        khachHangRepository.save(kh);

        TaiKhoan tk = new TaiKhoan();
        tk.setMaTK(generateNewMATK());
        tk.setEmailTK(dto.getEmail());
        tk.setMatKhau(DigestUtils.md5DigestAsHex(dto.getPassword().getBytes()).toUpperCase());
        tk.setLoaiTaiKhoan("Khách hàng");
        tk.setMaKH(kh.getMaKH());
        taiKhoanRepository.save(tk);

        return "redirect:/tai-khoan/dang-nhap";
    }

    @GetMapping("/dang-xuat")
    public String dangXuat(HttpSession session) {
        session.invalidate();
        return "redirect:/trang-chu";
    }

    @GetMapping("/thong-tin-tai-khoan")
    public String thongTinTaiKhoan(HttpSession session, Model model) {
        String maTK = (String) session.getAttribute("UserID");
        if (maTK == null) return "redirect:/tai-khoan/dang-nhap";

        TaiKhoan tk = taiKhoanRepository.findById(maTK).orElse(null);
        KhachHang kh = (tk != null && tk.getMaKH() != null) ? khachHangRepository.findById(tk.getMaKH()).orElse(new KhachHang()) : new KhachHang();
        
        model.addAttribute("khachHang", kh);
        return "taikhoan/ThongTinTaiKhoan";
    }

    @PostMapping("/thong-tin-tai-khoan")
    public String capNhatThongTin(@ModelAttribute KhachHang khForm, HttpSession session, RedirectAttributes ra) {
        String maTK = (String) session.getAttribute("UserID");
        if (maTK == null) return "redirect:/tai-khoan/dang-nhap";

        KhachHang kh = khachHangRepository.findById(khForm.getMaKH()).orElse(null);
        if (kh != null) {
            kh.setTenKH(khForm.getTenKH());
            kh.setSdtKH(khForm.getSdtKH());
            kh.setDiaChiKH(khForm.getDiaChiKH());
            khachHangRepository.save(kh);
            ra.addFlashAttribute("success", "Cập nhật thông tin thành công!");
        }
        return "redirect:/tai-khoan/thong-tin-tai-khoan";
    }

    @GetMapping("/lich-su-don-hang")
    public String lichSuDonHang(HttpSession session, Model model) {
        String maTK = (String) session.getAttribute("UserID");
        if (maTK == null) return "redirect:/tai-khoan/dang-nhap";

        TaiKhoan tk = taiKhoanRepository.findById(maTK).orElse(null);
        List<HoaDon> listDonHang = (tk != null && tk.getMaKH() != null) ? hoaDonRepository.findByKhachHang_MaKHOrderByNgayLapDesc(tk.getMaKH()) : new ArrayList<>();
        
        model.addAttribute("listDonHang", listDonHang);
        return "taikhoan/LichSuDonHang";
    }

    @GetMapping("/chi-tiet-don-hang/{id}")
    public String chiTietDonHang(@PathVariable("id") String id, HttpSession session, Model model) {
        String maTK = (String) session.getAttribute("UserID");
        if (maTK == null) return "redirect:/tai-khoan/dang-nhap";

        HoaDon donHang = hoaDonRepository.findById(id).orElse(null);
        TaiKhoan user = taiKhoanRepository.findById(maTK).orElse(null);
        
        if (donHang == null || user == null || !user.getMaKH().equals(donHang.getKhachHang().getMaKH())) {
            return "redirect:/tai-khoan/lich-su-don-hang";
        }

        model.addAttribute("donHang", donHang);
        return "taikhoan/ChiTietDonHang";
    }

    // NGHIỆP VỤ HỦY ĐƠN: HOÀN TIỀN 100% (QR/TIỀN MẶT) & HOÀN TỒN KHO SẢN PHẨM
    @GetMapping("/huy-don")
    @Transactional
    public String huyDonHang(@RequestParam("maHD") String maHD, HttpSession session, RedirectAttributes ra) {
        String maTK = (String) session.getAttribute("UserID");
        if (maTK == null) return "redirect:/tai-khoan/dang-nhap";

        HoaDon hoaDon = hoaDonRepository.findById(maHD).orElse(null);
        if (hoaDon == null) {
            ra.addFlashAttribute("error", "Đơn hàng không tồn tại.");
            return "redirect:/tai-khoan/lich-su-don-hang";
        }

        String trangThai = hoaDon.getTrangThaiTT();
        if (!"Đã đặt".equals(trangThai) && !"Đã thanh toán".equals(trangThai)) {
            ra.addFlashAttribute("error", "Đơn hàng đã vận chuyển hoặc hoàn thành, không thể hủy.");
            return "redirect:/tai-khoan/lich-su-don-hang";
        }

        // Tạo thông báo hoàn tiền theo phương thức thanh toán
        if ("Đã thanh toán".equals(trangThai)) {
            ra.addFlashAttribute("success", "Hủy thành công! Hệ thống hoàn lại 100% số tiền (" + String.format("%,.0f", hoaDon.getTongTienHD()) + " đ) vào tài khoản.");
        } else {
            ra.addFlashAttribute("success", "Hủy đơn hàng thành công (Đơn hàng COD không tính tiền).");
        }

        // Hoàn trả lại số lượng sản phẩm mua về lại kho tồn
        List<ChiTietHoaDon> chiTiets = chiTietHoaDonRepository.findByMaHD(maHD);
        for (ChiTietHoaDon ct : chiTiets) {
            SanPham sp = sanPhamRepository.findById(ct.getMaSP()).orElse(null);
            if (sp != null) {
                try {
                    int soLuongMua = Integer.parseInt(ct.getSoLuongSP_HD().trim().replace(",", ""));
                    sp.setSoLuongTon((sp.getSoLuongTon() != null ? sp.getSoLuongTon() : 0) + soLuongMua);
                    sanPhamRepository.save(sp);
                } catch (Exception e) {}
            }
        }

        hoaDon.setTrangThaiTT("Đã hủy");
        hoaDonRepository.save(hoaDon);
        return "redirect:/tai-khoan/lich-su-don-hang";
    }

    private String generateNewMATK() {
        TaiKhoan lastTK = taiKhoanRepository.findFirstByMaTKStartingWithOrderByMaTKDesc("TK_KH");
        if (lastTK == null) return "TK_KH001";
        try {
            return String.format("TK_KH%03d", Integer.parseInt(lastTK.getMaTK().substring(5)) + 1);
        } catch (Exception e) {
            return "TK_KH" + System.currentTimeMillis() % 1000;
        }
    }

    private String generateMaKH() {
        KhachHang lastKH = khachHangRepository.findFirstByMaKHStartingWithOrderByMaKHDesc("KH");
        if (lastKH == null) return "KH001";
        try {
            return String.format("KH%03d", Integer.parseInt(lastKH.getMaKH().substring(2)) + 1);
        } catch (Exception e) {
            return "KH" + System.currentTimeMillis() % 1000;
        }
    }
}