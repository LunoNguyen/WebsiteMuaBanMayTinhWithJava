package com.nhom8.DoAnJava.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

import com.nhom8.DoAnJava.dto.DangKyDTO;
import com.nhom8.DoAnJava.dto.DangNhapDTO;
import com.nhom8.DoAnJava.dto.ItemGioHangDTO;
import com.nhom8.DoAnJava.model.GioHang;
import com.nhom8.DoAnJava.model.HoaDon;
import com.nhom8.DoAnJava.model.KhachHang;
import com.nhom8.DoAnJava.model.SanPham;
import com.nhom8.DoAnJava.model.TaiKhoan;
import com.nhom8.DoAnJava.repository.GioHangRepository;
import com.nhom8.DoAnJava.repository.HoaDonRepository;
import com.nhom8.DoAnJava.repository.KhachHangRepository;
import com.nhom8.DoAnJava.repository.SanPhamRepository;
import com.nhom8.DoAnJava.repository.TaiKhoanRepository;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/tai-khoan")
public class TaiKhoanController {

    @Autowired
    private TaiKhoanRepository taiKhoanRepository;

    @Autowired
    private GioHangRepository gioHangRepository;

    @Autowired
    private SanPhamRepository sanPhamRepository;

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Autowired
    private HoaDonRepository hoaDonRepository;

    // ==================== 1. ĐĂNG NHẬP & NẠP GIỎ HÀNG ====================
    @GetMapping("/dang-nhap")
    public String dangNhap(Model model) {
        model.addAttribute("dangNhapDTO", new DangNhapDTO());
        return "taikhoan/DangNhap";
    }

    @PostMapping("/xu-ly-dang-nhap")
    public String xuLyDangNhap(@Valid @ModelAttribute("dangNhapDTO") DangNhapDTO model, BindingResult bindingResult, HttpSession session, Model viewModel) {
        if (bindingResult.hasErrors()) {
            return "taikhoan/DangNhap";
        }

        String hashedPassword = DigestUtils.md5DigestAsHex(model.getPassword().getBytes()).toUpperCase();
        Optional<TaiKhoan> userOpt = taiKhoanRepository.findByEmailTKAndMatKhau(model.getEmail(), hashedPassword);

        if (userOpt.isPresent()) {
            TaiKhoan user = userOpt.get();
            session.setAttribute("UserID", user.getMaTK());
            session.setAttribute("UserEmail", user.getEmailTK());

            // NẠP GIỎ HÀNG TỪ DATABASE VÀO SESSION (Thay thế hoàn toàn đoạn C# cũ)
            List<GioHang> listGH_DB = gioHangRepository.findByMaTK(user.getMaTK());
            List<ItemGioHangDTO> lstGioHang = new ArrayList<>();
            
            for (GioHang gh : listGH_DB) {
                SanPham sp = sanPhamRepository.findById(gh.getMaSP()).orElse(null);
                if (sp != null) {
                    ItemGioHangDTO item = new ItemGioHangDTO(sp.getMaSP(), sp.getTenSP(), "no-image.jpg", sp.getDonGiaSP().doubleValue());
                    item.setiSoLuong(gh.getSoLuong());
                    lstGioHang.add(item);
                }
            }
            session.setAttribute("GioHang", lstGioHang);

            // PHÂN QUYỀN VÀ ĐIỀU HƯỚNG
            if ("Admin".equals(user.getLoaiTaiKhoan()) || "NhanVien".equals(user.getLoaiTaiKhoan())) {
                session.setAttribute("AccountType", user.getLoaiTaiKhoan());
                return "redirect:/admin/trang-admin"; 
            } else {
                session.setAttribute("AccountType", "Customer");
                if (user.getMaKH() != null) {
                    KhachHang kh = khachHangRepository.findById(user.getMaKH()).orElse(null);
                    if (kh != null) {
                        session.setAttribute("UserName", kh.getTenKH());
                    }
                }
                return "redirect:/trang-chu";
            }
        } else {
            viewModel.addAttribute("error", "Đăng nhập thất bại. Kiểm tra lại Email hoặc Mật khẩu.");
            return "taikhoan/DangNhap";
        }
    }

    // ==================== 2. ĐĂNG KÝ TÀI KHOẢN ====================
    @GetMapping("/dang-ky")
    public String dangKy(Model model) {
        model.addAttribute("dangKyDTO", new DangKyDTO());
        return "taikhoan/DangKy";
    }

    @PostMapping("/xu-ly-dang-ky")
    public String xuLyDangKy(@Valid @ModelAttribute("dangKyDTO") DangKyDTO model, BindingResult bindingResult, HttpSession session, Model viewModel) {
        if (bindingResult.hasErrors()) {
            return "taikhoan/DangKy";
        }

        if (!model.getPassword().equals(model.getConfirmPassword())) {
            viewModel.addAttribute("error", "Mật khẩu và xác nhận mật khẩu không khớp.");
            return "taikhoan/DangKy";
        }

        Optional<TaiKhoan> existingUser = taiKhoanRepository.findByEmailTK(model.getEmail());
        if (existingUser.isPresent()) {
            viewModel.addAttribute("error", "Email này đã được đăng ký.");
            return "DangKy";
        }

        TaiKhoan newUser = new TaiKhoan();
        newUser.setMaTK(generateNewMATK());
        newUser.setEmailTK(model.getEmail());
        newUser.setMatKhau(DigestUtils.md5DigestAsHex(model.getPassword().getBytes()).toUpperCase());
        newUser.setLoaiTaiKhoan("KhachHang");

        try {
            taiKhoanRepository.save(newUser);
            session.setAttribute("UserID", newUser.getMaTK());
            session.setAttribute("UserEmail", newUser.getEmailTK());
            session.setAttribute("AccountType", "Customer");
            return "redirect:/tai-khoan/thong-tin-tai-khoan";
        } catch (Exception ex) {
            viewModel.addAttribute("error", "Lỗi hệ thống: " + ex.getMessage());
            return "taikhoan/DangKy";
        }
    }

    // ==================== 3. ĐĂNG XUẤT ====================
    @GetMapping("/dang-xuat")
    public String dangXuat(HttpSession session) {
        session.invalidate(); // Xóa sạch toàn bộ Session
        return "redirect:/trang-chu";
    }

    // ==================== 4. THÔNG TIN CÁ NHÂN (HỒ SƠ) ====================
    @GetMapping("/thong-tin-tai-khoan")
    public String thongTinTaiKhoan(HttpSession session, Model model) {
        String maTK = (String) session.getAttribute("UserID");
        if (maTK == null) return "redirect:/tai-khoan/dang-nhap";

        TaiKhoan user = taiKhoanRepository.findById(maTK).orElse(null);
        KhachHang customer = null;

        if (user != null && user.getMaKH() != null) {
            customer = khachHangRepository.findById(user.getMaKH()).orElse(null);
        }

        if (customer == null) {
            customer = new KhachHang();
        }
        model.addAttribute("khachHang", customer);
        return "taikhoan/ThongTinTaiKhoan";
    }

    @PostMapping("/thong-tin-tai-khoan")
    public String capNhatThongTinTaiKhoan(@ModelAttribute("khachHang") KhachHang model, HttpSession session) {
        String maTK = (String) session.getAttribute("UserID");
        if (maTK == null) return "redirect:/tai-khoan/dang-nhap";

        TaiKhoan user = taiKhoanRepository.findById(maTK).orElse(null);
        KhachHang customer = null;

        if (user != null && user.getMaKH() != null) {
            customer = khachHangRepository.findById(user.getMaKH()).orElse(null);
        }

        // Nếu chưa có thông tin khách hàng (Lần đầu cập nhật)
        if (customer == null) {
            customer = new KhachHang();
            customer.setMaKH(generateMaKH());
            customer.setTenKH(model.getTenKH());
            customer.setSdtKH(model.getSdtKH());
            customer.setDiaChiKH(model.getDiaChiKH());
            customer.setEmailKH(user.getEmailTK());
            khachHangRepository.save(customer);

            // Cập nhật MAKH vào bảng TAIKHOAN
            user.setMaKH(customer.getMaKH());
            taiKhoanRepository.save(user);
        } else {
            // Đã có hồ sơ -> Chỉ cập nhật thông tin
            customer.setTenKH(model.getTenKH());
            customer.setSdtKH(model.getSdtKH());
            customer.setDiaChiKH(model.getDiaChiKH());
            khachHangRepository.save(customer);
        }

        session.setAttribute("UserName", customer.getTenKH());
        return "redirect:/trang-chu";
    }

    // ==================== 5. LỊCH SỬ MUA HÀNG ====================
    @GetMapping("/lich-su-don-hang")
    public String lichSuDonHang(HttpSession session, Model model) {
        // 1. Kiểm tra đăng nhập
        String maTK = (String) session.getAttribute("UserID");
        if (maTK == null) {
            return "redirect:/tai-khoan/dang-nhap";
        }

        // 2. Lấy thông tin tài khoản và khách hàng
        TaiKhoan user = taiKhoanRepository.findById(maTK).orElse(null);
        
        if (user != null && user.getMaKH() != null) {
            // 3. GỌI HÀM LẤY DANH SÁCH (LIST) Ở BƯỚC 1
            List<HoaDon> listDonHang = hoaDonRepository.findByKhachHang_MaKHOrderByNgayLapDesc(user.getMaKH());
            
            // 4. Đẩy danh sách ra View (Tên biến phải khớp với ${listDonHang} trong HTML)
            model.addAttribute("listDonHang", listDonHang);
        }

        return "taikhoan/LichSuDonHang";
    }

    @GetMapping("/chi-tiet-don-hang/{id}")
    public String chiTietDonHang(@PathVariable("id") String id, HttpSession session, Model model) {
        String maTK = (String) session.getAttribute("UserID");
        if (maTK == null) return "redirect:/tai-khoan/dang-nhap";

        HoaDon donHang = hoaDonRepository.findById(id).orElse(null);
        TaiKhoan user = taiKhoanRepository.findById(maTK).orElse(null);
        
        // Bảo mật: Chỉ cho phép xem nếu hóa đơn thuộc về chính Khách hàng này
        if (donHang == null || user == null || !user.getMaKH().equals(donHang.getKhachHang().getMaKH())) {
            return "redirect:/tai-khoan/lich-su-don-hang";
        }

        model.addAttribute("donHang", donHang);
        return "taikhoan/ChiTietDonHang";
    }

    // ==================== 6. HÀM TIỆN ÍCH (TẠO MÃ TỰ ĐỘNG) ====================
    private String generateNewMATK() {
        TaiKhoan lastTK = taiKhoanRepository.findFirstByMaTKStartingWithOrderByMaTKDesc("TK_KH");
        if (lastTK == null) return "TK_KH001";
        
        String lastId = lastTK.getMaTK().substring(5); // Cắt bỏ chữ "TK_KH"
        try {
            int nextId = Integer.parseInt(lastId) + 1;
            return String.format("TK_KH%03d", nextId);
        } catch (Exception e) {
            return "TK_KH" + System.currentTimeMillis();
        }
    }

    private String generateMaKH() {
        KhachHang lastKH = khachHangRepository.findFirstByMaKHStartingWithOrderByMaKHDesc("KH");
        if (lastKH == null) return "KH001";
        
        String lastId = lastKH.getMaKH().substring(2); // Cắt bỏ chữ "KH"
        try {
            int nextId = Integer.parseInt(lastId) + 1;
            return String.format("KH%03d", nextId);
        } catch (Exception e) {
            return "KH" + System.currentTimeMillis();
        }
    }
}