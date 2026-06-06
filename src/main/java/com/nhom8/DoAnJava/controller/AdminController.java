package com.nhom8.DoAnJava.controller;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.Map;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.nhom8.DoAnJava.model.KhachHang;
import com.nhom8.DoAnJava.model.MoTa;
import com.nhom8.DoAnJava.model.NhanVien;
import com.nhom8.DoAnJava.model.SanPham;
import com.nhom8.DoAnJava.model.TaiKhoan;
import com.nhom8.DoAnJava.model.CapNhatGia;
import com.nhom8.DoAnJava.model.DanhSachAnh;
import com.nhom8.DoAnJava.repository.DanhSachAnhRepository;
import com.nhom8.DoAnJava.repository.CapNhatGiaRepository;
import com.nhom8.DoAnJava.repository.HoaDonRepository;
import com.nhom8.DoAnJava.repository.KhachHangRepository;
import com.nhom8.DoAnJava.repository.LoaiSanPhamRepository;
import com.nhom8.DoAnJava.repository.NhaCungCapRepository;
import com.nhom8.DoAnJava.repository.NhaSanXuatRepository;
import com.nhom8.DoAnJava.repository.NhanVienRepository;
import com.nhom8.DoAnJava.repository.SanPhamRepository;
import com.nhom8.DoAnJava.repository.TaiKhoanRepository;
import com.nhom8.DoAnJava.service.AdminService;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired private KhachHangRepository khachHangRepository; 
    @Autowired private NhanVienRepository nhanVienRepository;
    @Autowired private HoaDonRepository hoaDonRepository;
    @Autowired private TaiKhoanRepository taiKhoanRepository;
    @Autowired private AdminService adminService;
    @Autowired private SanPhamRepository sanPhamRepository;
    @Autowired private LoaiSanPhamRepository loaiSanPhamRepository;
    @Autowired private NhaSanXuatRepository nhaSanXuatRepository;
    @Autowired private NhaCungCapRepository nhaCungCapRepository;
    @Autowired private CapNhatGiaRepository capNhatGiaRepository;
    @Autowired private DanhSachAnhRepository danhSachAnhRepository;

    // 1. TRANG DASHBOARD ADMIN MAIN
    @GetMapping("/trang-admin")
    public String trangAdmin(
            @RequestParam(value = "thang", required = false) Integer thang,
            @RequestParam(value = "nam", required = false) Integer nam,
            Model model) {

        int currentMonth = (thang != null) ? thang : LocalDate.now().getMonthValue();
        int currentYear = (nam != null) ? nam : LocalDate.now().getYear();

        Map<String, Object> stats = adminService.getDashboardStats(currentMonth, currentYear);
        
        model.addAttribute("Thang", currentMonth);
        model.addAttribute("Nam", currentYear);
        model.addAllAttributes(stats); // Đẩy tự động toàn bộ thống kê vào Model

        return "Admin/TrangAdmin";
    }

    // 2. QUẢN LÝ DANH SÁCH SẢN PHẨM
    @GetMapping("/ql-sanpham")
    public String quanLySanPham(Model model) {
        model.addAttribute("QLSP", sanPhamRepository.findAll());
        return "Admin/AD_QLSP";
    }

    // 3. QUẢN LÝ DANH SÁCH KHÁCH HÀNG
    @GetMapping("/ql-khachhang")
    public String quanLyKhachHang(Model model) {
        // Lấy danh sách khách hàng từ DB (giả định bạn đã có khachHangRepository)
        model.addAttribute("QLKH", khachHangRepository.findAll());
        
        return "Admin/AD_QLKH"; 
    }

    // 5. QUẢN LÝ DANH SÁCH NHÂN VIÊN
    @GetMapping("/ql-nhanvien")
    public String quanLyNhanVien(Model model) {
        model.addAttribute("QLNV", nhanVienRepository.findAll());
        return "Admin/AD_QLNV"; 
    }

    // 6. QUẢN LÝ DANH SÁCH ĐƠN HÀNG
    @GetMapping("/ql-donhang")
    public String quanLyDonHang(Model model) {
        // Lưu ý: Trong file giao diện QL_DonHang.html, chúng ta đang dùng biến ${listDH}
        model.addAttribute("listDH", hoaDonRepository.findAll());
        return "Admin/QL_DonHang"; 
    }

    // 7. QUẢN LÝ DANH SÁCH TÀI KHOẢN
    @GetMapping("/ql-taikhoan")
    public String quanLyTaiKhoan(Model model) {
        model.addAttribute("QLTK", taiKhoanRepository.findAll());
        return "Admin/AD_QLTK"; 
    }

    // ==========================================
    // THÊM SẢN PHẨM TRỰC TIẾP TỪ NÚT "THÊM MỚI"
    // ==========================================
    @GetMapping("/create-sp")
    public String createSanPhamView(Model model) {
        // Đẩy danh sách ra form để chọn Dropdown
        model.addAttribute("dsLoai", loaiSanPhamRepository.findAll());
        model.addAttribute("dsNSX", nhaSanXuatRepository.findAll());
        model.addAttribute("dsNCC", nhaCungCapRepository.findAll());
        return "Admin/Create_SP";
    }

    @PostMapping("/create-sp")
    @Transactional
    public String createSanPhamPost(
            @ModelAttribute SanPham sanPham,
            @RequestParam(value = "fileAnhs", required = false) MultipartFile[] fileAnhs,
            RedirectAttributes redirectAttributes) {

        try {
            String maSP = adminService.generateMaSP();
            LocalDateTime ngayCN = LocalDateTime.now();

            CapNhatGia capNhatGia = new CapNhatGia();
            capNhatGia.setNgayCN(ngayCN);
            capNhatGia.setDonGiaCN(sanPham.getDonGiaSP());
            capNhatGiaRepository.save(capNhatGia);

            sanPham.setMaSP(maSP);
            sanPham.setNgayCN(ngayCN);

            if (sanPham.getSoLuongTon() == null) {
                sanPham.setSoLuongTon(0);
            }

            if (sanPham.getDonVT() == null || sanPham.getDonVT().trim().isEmpty()) {
                sanPham.setDonVT("Cái");
            }

            sanPhamRepository.save(sanPham);

            if (fileAnhs != null && fileAnhs.length > 0) {
                Path uploadDir = Paths.get("src/main/resources/static/Content/HinhAnhPhongVu");
                Files.createDirectories(uploadDir);

                int index = 1;

                for (MultipartFile file : fileAnhs) {
                    if (file == null || file.isEmpty()) {
                        continue;
                    }

                    String originalName = file.getOriginalFilename();
                    String extension = layDuoiFile(originalName);

                    String tenAnh = maSP + "_" + String.format("%02d", index) + extension;

                    Path filePath = uploadDir.resolve(tenAnh);
                    Files.write(filePath, file.getBytes());

                    DanhSachAnh anh = new DanhSachAnh();
                    anh.setMaDsa(generateMaDSA());
                    anh.setSanPham(sanPham);
                    anh.setTenAnh(tenAnh);

                    danhSachAnhRepository.save(anh);

                    index++;
                }
            }

            redirectAttributes.addFlashAttribute(
                    "Success",
                    "Thêm sản phẩm thành công! Mã sản phẩm: " + maSP
            );

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "Error",
                    "Lỗi thêm sản phẩm: " + e.getMessage()
            );
        }

        return "redirect:/admin/ql-sanpham";
    }

    // ==========================================
    // CHỈNH SỬA SẢN PHẨM
    // ==========================================
    @GetMapping("/edit-sp/{id}")
    public String editSanPhamView(@PathVariable("id") String id, Model model, RedirectAttributes redirectAttributes) {
        SanPham sp = sanPhamRepository.findById(id).orElse(null);
        if (sp == null) {
            redirectAttributes.addFlashAttribute("Error", "Không tìm thấy sản phẩm!");
            return "redirect:/admin/ql-sanpham";
        }
        
        // Đẩy dữ liệu cũ và danh sách Dropdown ra View
        model.addAttribute("sanPham", sp);
        model.addAttribute("dsLoai", loaiSanPhamRepository.findAll());
        model.addAttribute("dsNSX", nhaSanXuatRepository.findAll());
        model.addAttribute("dsNCC", nhaCungCapRepository.findAll());
        
        return "Admin/Edit_SP";
    }

    @PostMapping("/edit-sp")
    public String editSanPhamPost(@ModelAttribute SanPham sanPhamMoi, RedirectAttributes redirectAttributes) {
        try {
            SanPham spCu = sanPhamRepository.findById(sanPhamMoi.getMaSP()).orElse(null);
            if(spCu != null) {
                // Cập nhật các trường từ Form (Giữ lại các trường không sửa như Ảnh)
                spCu.setTenSP(sanPhamMoi.getTenSP());
                spCu.setDonGiaSP(sanPhamMoi.getDonGiaSP());
                spCu.setSoLuongTon(sanPhamMoi.getSoLuongTon());
                spCu.setDonVT(sanPhamMoi.getDonVT());
                spCu.setMaLoai(sanPhamMoi.getMaLoai());
                spCu.setMaNCC(sanPhamMoi.getMaNCC());
                spCu.setMaNSX(sanPhamMoi.getMaNSX());
                
                sanPhamRepository.save(spCu);
                redirectAttributes.addFlashAttribute("Success", "Cập nhật sản phẩm thành công!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("Error", "Lỗi cập nhật: " + e.getMessage());
        }
        return "redirect:/admin/ql-sanpham";
    }

    // ==========================================
    // THÊM KHÁCH HÀNG TRỰC TIẾP TỪ ADMIN
    // ==========================================
    @GetMapping("/create-kh")
    public String createKhachHangView(Model model) {
        model.addAttribute("khachHang", new KhachHang());
        return "Admin/Create_KH"; 
    }

    @PostMapping("/create-kh")
    public String createKhachHangPost(@ModelAttribute KhachHang khachHang, RedirectAttributes redirectAttributes) {
        try {
            // Lưu trực tiếp xuống Database
            khachHangRepository.save(khachHang);
            redirectAttributes.addFlashAttribute("Success", "Thêm khách hàng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("Error", "Lỗi thêm khách hàng: " + e.getMessage());
        }
        return "redirect:/admin/ql-khachhang";
    }

    // ==========================================
    // CHỈNH SỬA THÔNG TIN KHÁCH HÀNG
    // ==========================================
    @GetMapping("/edit-kh/{id}")
    public String editKhachHangView(@PathVariable("id") String id, Model model, RedirectAttributes redirectAttributes) {
        // Truy xuất dữ liệu khách hàng từ Database có sẵn để đổ lên Form
        KhachHang kh = khachHangRepository.findById(id).orElse(null);
        if (kh == null) {
            redirectAttributes.addFlashAttribute("Error", "Không tìm thấy khách hàng!");
            return "redirect:/admin/ql-khachhang";
        }
        
        model.addAttribute("khachHang", kh);
        return "Admin/Edit_KH";
    }

    @PostMapping("/edit-kh")
    public String editKhachHangPost(@ModelAttribute KhachHang khMoi, RedirectAttributes redirectAttributes) {
        try {
            KhachHang khCu = khachHangRepository.findById(khMoi.getMaKH()).orElse(null);
            if(khCu != null) {
                // Cập nhật các trường thông tin từ Form gửi lên
                khCu.setTenKH(khMoi.getTenKH());
                khCu.setSdtKH(khMoi.getSdtKH());
                khCu.setDiaChiKH(khMoi.getDiaChiKH());
                khCu.setEmailKH(khMoi.getEmailKH());
                
                // Lưu đè bản ghi đã cập nhật xuống DB
                khachHangRepository.save(khCu);
                redirectAttributes.addFlashAttribute("Success", "Cập nhật khách hàng thành công!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("Error", "Lỗi cập nhật: " + e.getMessage());
        }
        return "redirect:/admin/ql-khachhang";
    }

    // ==========================================
    // XÓA KHÁCH HÀNG (CÓ BẮT LỖI KHÓA NGOẠI)
    // ==========================================
    @PostMapping("/xoa-khachhang/{id}")
    public String xoaKhachHang(@PathVariable("id") String id, RedirectAttributes redirectAttributes) {
        try {
            khachHangRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("Success", "Xóa khách hàng thành công!");
        } catch (Exception ex) {
            // Chốt chặn an toàn: Tránh lỗi sập web khi khách hàng đã có đơn hàng
            redirectAttributes.addFlashAttribute("Error", "Lỗi Khóa Ngoại! Khách hàng này đã có hóa đơn trong hệ thống, không thể xóa.");
        }
        return "redirect:/admin/ql-khachhang";
    }

    // ==========================================
    // THÊM NHÂN VIÊN MỚI
    // ==========================================
    @GetMapping("/create-nv")
    public String createNhanVienView(Model model) {
        model.addAttribute("nhanVien", new NhanVien());
        return "Admin/Create_NV"; 
    }

    @PostMapping("/create-nv")
    public String createNhanVienPost(@ModelAttribute NhanVien nhanVien, RedirectAttributes redirectAttributes) {
        try {
            // Lưu dữ liệu trực tiếp vào database sẵn có
            nhanVienRepository.save(nhanVien);
            redirectAttributes.addFlashAttribute("Success", "Thêm nhân viên thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("Error", "Lỗi thêm nhân viên: " + e.getMessage());
        }
        return "redirect:/admin/ql-nhanvien";
    }

    // ==========================================
    // CHỈNH SỬA THÔNG TIN NHÂN VIÊN
    // ==========================================
    @GetMapping("/edit-nv/{id}")
    public String editNhanVienView(@PathVariable("id") String id, Model model, RedirectAttributes redirectAttributes) {
        // Truy xuất dữ liệu nhân viên từ database sẵn có
        NhanVien nv = nhanVienRepository.findById(id).orElse(null);
        if (nv == null) {
            redirectAttributes.addFlashAttribute("Error", "Không tìm thấy nhân viên!");
            return "redirect:/admin/ql-nhanvien";
        }
        
        model.addAttribute("nhanVien", nv);
        return "Admin/Edit_NV";
    }

    @PostMapping("/edit-nv")
    public String editNhanVienPost(@ModelAttribute NhanVien nvMoi, RedirectAttributes redirectAttributes) {
        try {
            NhanVien nvCu = nhanVienRepository.findById(nvMoi.getMaNV()).orElse(null);
            if(nvCu != null) {
                // Cập nhật các trường thông tin từ Form gửi lên
                nvCu.setTenNV(nvMoi.getTenNV());
                nvCu.setSdtNV(nvMoi.getSdtNV());
                nvCu.setDiaChiNV(nvMoi.getDiaChiNV());
                nvCu.setEmailNV(nvMoi.getEmailNV());
                // (Bạn có thể bổ sung thêm nvCu.setChucVu(...) nếu bảng NhanVien có trường này)
                
                // Lưu lại thông tin cập nhật xuống DB
                nhanVienRepository.save(nvCu);
                redirectAttributes.addFlashAttribute("Success", "Cập nhật nhân viên thành công!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("Error", "Lỗi cập nhật: " + e.getMessage());
        }
        return "redirect:/admin/ql-nhanvien";
    }

    // ==========================================
    // XÓA NHÂN VIÊN (BẮT LỖI KHÓA NGOẠI)
    // ==========================================
    @PostMapping("/xoa-nhanvien/{id}")
    public String xoaNhanVien(@PathVariable("id") String id, RedirectAttributes redirectAttributes) {
        try {
            nhanVienRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("Success", "Xóa nhân viên thành công!");
        } catch (Exception ex) {
            // Chốt chặn: Tránh sập web nếu nhân viên này đang lập hóa đơn hoặc được liên kết với tài khoản
            redirectAttributes.addFlashAttribute("Error", "Lỗi Khóa Ngoại! Nhân viên này đã liên kết với các dữ liệu khác trong hệ thống, không thể xóa.");
        }
        return "redirect:/admin/ql-nhanvien";
    }

    // ==========================================
    // THÊM TÀI KHOẢN MỚI
    // ==========================================
    @GetMapping("/create-tk")
    public String createTaiKhoanView(Model model) {
        model.addAttribute("taiKhoan", new TaiKhoan());
        
        // Đẩy danh sách Nhân viên và Khách hàng ra form để chọn Dropdown
        model.addAttribute("dsNhanVien", nhanVienRepository.findAll());
        model.addAttribute("dsKhachHang", khachHangRepository.findAll());
        return "Admin/Create_TK"; 
    }

    @PostMapping("/create-tk")
    public String createTaiKhoanPost(@ModelAttribute TaiKhoan taiKhoan, RedirectAttributes redirectAttributes) {
        try {
            // Quan trọng: Mã hóa MD5 mật khẩu (có getBytes() để tránh lỗi) trước khi lưu
            if (taiKhoan.getMatKhau() != null && !taiKhoan.getMatKhau().isEmpty()) {
                String hashedPass = org.springframework.util.DigestUtils.md5DigestAsHex(taiKhoan.getMatKhau().getBytes()).toUpperCase();
                taiKhoan.setMatKhau(hashedPass);
            }

            taiKhoanRepository.save(taiKhoan);
            redirectAttributes.addFlashAttribute("Success", "Thêm tài khoản thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("Error", "Lỗi thêm tài khoản: " + e.getMessage());
        }
        return "redirect:/admin/ql-taikhoan";
    }

    // ==========================================
    // CHỈNH SỬA TÀI KHOẢN
    // ==========================================
    @GetMapping("/edit-tk/{id}")
    public String editTaiKhoanView(@PathVariable("id") String id, Model model, RedirectAttributes redirectAttributes) {
        TaiKhoan tk = taiKhoanRepository.findById(id).orElse(null);
        if (tk == null) {
            redirectAttributes.addFlashAttribute("Error", "Không tìm thấy tài khoản!");
            return "redirect:/admin/ql-taikhoan";
        }
        
        model.addAttribute("taiKhoan", tk);
        model.addAttribute("dsNhanVien", nhanVienRepository.findAll());
        model.addAttribute("dsKhachHang", khachHangRepository.findAll());
        return "Admin/Edit_TK";
    }

    @PostMapping("/edit-tk")
    public String editTaiKhoanPost(@ModelAttribute TaiKhoan tkMoi, RedirectAttributes redirectAttributes) {
        try {
            TaiKhoan tkCu = taiKhoanRepository.findById(tkMoi.getMaTK()).orElse(null);
            if(tkCu != null) {
                // Cập nhật thông tin phân quyền
                tkCu.setEmailTK(tkMoi.getEmailTK());
                tkCu.setLoaiTaiKhoan(tkMoi.getLoaiTaiKhoan());
                tkCu.setMaNV(tkMoi.getMaNV());
                tkCu.setMaKH(tkMoi.getMaKH());
                
                // Chỉ mã hóa và cập nhật mật khẩu nếu Admin có nhập mật khẩu mới vào Form
                if (tkMoi.getMatKhau() != null && !tkMoi.getMatKhau().trim().isEmpty()) {
                    String hashedPass = org.springframework.util.DigestUtils.md5DigestAsHex(tkMoi.getMatKhau().getBytes()).toUpperCase();
                    tkCu.setMatKhau(hashedPass);
                }
                
                taiKhoanRepository.save(tkCu);
                redirectAttributes.addFlashAttribute("Success", "Cập nhật tài khoản thành công!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("Error", "Lỗi cập nhật: " + e.getMessage());
        }
        return "redirect:/admin/ql-taikhoan";
    }

    // ==========================================
    // XÓA TÀI KHOẢN
    // ==========================================
    @PostMapping("/xoa-taikhoan/{id}")
    public String xoaTaiKhoan(@PathVariable("id") String id, RedirectAttributes redirectAttributes) {
        try {
            taiKhoanRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("Success", "Xóa tài khoản thành công!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("Error", "Lỗi! Tài khoản này đang được liên kết ràng buộc, không thể xóa.");
        }
        return "redirect:/admin/ql-taikhoan";
    }

    // 8. GIAO DIỆN NHẬP HÀNG (THỦ CÔNG / CSV)
    @GetMapping("/nhap-hang")
    public String nhapHangView(Model model) {
        model.addAttribute("sanPham", new SanPham());
        model.addAttribute("moTa", new MoTa());
        model.addAttribute("MaSPSapTao", adminService.generateMaSP());
        return "Admin/NhapHang";
    }

    // CÁCH 1: XỬ LÝ NHẬP THỦ CÔNG
    @PostMapping("/nhap-thu-cong")
    public String xuLyNhapThuCong(@ModelAttribute SanPham sp, 
                                  @ModelAttribute MoTa mt, 
                                  @RequestParam("fileAnh") MultipartFile fileAnh,
                                  RedirectAttributes redirectAttributes) {
        try {
            adminService.nhapHangThuCong(sp, mt, fileAnh);
            redirectAttributes.addFlashAttribute("Success", "Thêm sản phẩm thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("Error", "Lỗi nhập hàng: " + e.getMessage());
        }
        return "redirect:nhap-hang";
    }

    // CÁCH 2: XỬ LÝ NHẬP FILE CSV
    @PostMapping("/nhap-file")
    public String xuLyNhapTuFile(@RequestParam("fileCSV") MultipartFile fileCSV, 
                                  RedirectAttributes redirectAttributes) {
        if (fileCSV.isEmpty() || !fileCSV.getOriginalFilename().endsWith(".csv")) {
            redirectAttributes.addFlashAttribute("Error", "File không hợp lệ. Vui lòng chọn file .csv");
            return "redirect:nhap-hang";
        }
        try {
            Map<String, Integer> res = adminService.nhapHangTuCSV(fileCSV);
            redirectAttributes.addFlashAttribute("Success", 
                String.format("Hoàn tất! Đã thêm mới %d SP và Cập nhật kho %d SP.", res.get("new"), res.get("update")));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("Error", "Lỗi đọc file: " + e.getMessage());
        }
        return "redirect:nhap-hang";
    }

    // 9. THAO TÁC XÓA SẢN PHẨM (HỖ TRỢ BẮT LỖI KHÓA NGOẠI)
    @PostMapping("/xoa-sanpham/{id}")
    public String xoaSanPham(@PathVariable("id") String id, RedirectAttributes redirectAttributes) {
        try {
            sanPhamRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("Success", "Xóa sản phẩm thành công!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("Error", "Lỗi Khóa Ngoại! Sản phẩm này đã tồn tại trong Chi Tiết Đơn Hàng!");
        }
        return "redirect:ql-sanpham";
    }

    @GetMapping("/details-sp/{id}")
    public String detailsSanPhamView(
            @PathVariable("id") String id,
            Model model,
            RedirectAttributes redirectAttributes) {

        SanPham sp = sanPhamRepository.findById(id).orElse(null);

        if (sp == null) {
            redirectAttributes.addFlashAttribute("Error", "Không tìm thấy sản phẩm!");
            return "redirect:/admin/ql-sanpham";
        }

        model.addAttribute("sanPham", sp);
        return "Admin/Details_SP";
    }

    @GetMapping("/mota-sp/{id}")
    public String moTaSanPhamView(
            @PathVariable("id") String id,
            Model model,
            RedirectAttributes redirectAttributes) {

        SanPham sp = sanPhamRepository.findById(id).orElse(null);

        if (sp == null) {
            redirectAttributes.addFlashAttribute("Error", "Không tìm thấy sản phẩm!");
            return "redirect:/admin/ql-sanpham";
        }

        model.addAttribute("sanPham", sp);
        return "Admin/Details_SP";
    }

    @GetMapping("/delete-sp/{id}")
    public String deleteSanPhamView(
            @PathVariable("id") String id,
            Model model,
            RedirectAttributes redirectAttributes) {

        SanPham sp = sanPhamRepository.findById(id).orElse(null);

        if (sp == null) {
            redirectAttributes.addFlashAttribute("Error", "Không tìm thấy sản phẩm!");
            return "redirect:/admin/ql-sanpham";
        }

        model.addAttribute("sanPham", sp);
        return "Admin/Delete_SP";
    }

    @PostMapping("/delete-sp")
    public String deleteSanPhamPost(
            @RequestParam("maSP") String maSP,
            RedirectAttributes redirectAttributes) {

        try {
            sanPhamRepository.deleteById(maSP);
            redirectAttributes.addFlashAttribute("Success", "Xóa sản phẩm thành công!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute(
                    "Error",
                    "Không thể xóa sản phẩm này vì đã liên kết với hóa đơn hoặc dữ liệu khác."
            );
        }

        return "redirect:/admin/ql-sanpham";
    }

    private String layDuoiFile(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return ".jpg";
        }

        String extension = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();

        if (
                !extension.equals(".jpg")
                        && !extension.equals(".jpeg")
                        && !extension.equals(".png")
                        && !extension.equals(".webp")
        ) {
            return ".jpg";
        }

        return extension;
    }

    private synchronized String generateMaDSA() {
        DanhSachAnh last = danhSachAnhRepository.findTopByOrderByMaDsaDesc();

        if (last == null || last.getMaDsa() == null) {
            return "DSA001";
        }

        try {
            int next = Integer.parseInt(last.getMaDsa().substring(3)) + 1;
            return String.format("DSA%03d", next);
        } catch (Exception e) {
            return "DSA" + System.currentTimeMillis() % 1000000;
        }
    }
}