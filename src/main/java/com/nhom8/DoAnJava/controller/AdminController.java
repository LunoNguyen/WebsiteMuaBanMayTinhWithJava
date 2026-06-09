package com.nhom8.DoAnJava.controller;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.nhom8.DoAnJava.model.CapNhatGia;
import com.nhom8.DoAnJava.model.ChiTietHoaDon;
import com.nhom8.DoAnJava.model.DanhSachAnh;
import com.nhom8.DoAnJava.model.HoaDon;
import com.nhom8.DoAnJava.model.KhachHang;
import com.nhom8.DoAnJava.model.KhuyenMai;
import com.nhom8.DoAnJava.model.MoTa;
import com.nhom8.DoAnJava.model.NhaCungCap;
import com.nhom8.DoAnJava.model.NhanVien;
import com.nhom8.DoAnJava.model.PhieuNhapHang;
import com.nhom8.DoAnJava.model.SanPham;
import com.nhom8.DoAnJava.model.TaiKhoan;
import com.nhom8.DoAnJava.repository.CapNhatGiaRepository;
import com.nhom8.DoAnJava.repository.ChiTietHoaDonRepository;
import com.nhom8.DoAnJava.repository.ChucVuRepository;
import com.nhom8.DoAnJava.repository.DanhSachAnhRepository;
import com.nhom8.DoAnJava.repository.GioHangRepository;
import com.nhom8.DoAnJava.repository.HoaDonRepository;
import com.nhom8.DoAnJava.repository.KhachHangRepository;
import com.nhom8.DoAnJava.repository.KhuyenMaiRepository;
import com.nhom8.DoAnJava.repository.LoaiSanPhamRepository;
import com.nhom8.DoAnJava.repository.MoTaRepository;
import com.nhom8.DoAnJava.repository.NhaCungCapRepository;
import com.nhom8.DoAnJava.repository.NhaSanXuatRepository;
import com.nhom8.DoAnJava.repository.NhanVienRepository;
import com.nhom8.DoAnJava.repository.PhieuNhapHangRepository;
import com.nhom8.DoAnJava.repository.SanPhamRepository;
import com.nhom8.DoAnJava.repository.TaiKhoanRepository;
import com.nhom8.DoAnJava.service.AdminService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {

    // Ảnh sẽ lưu trực tiếp vào thư mục tên "uploads/HinhAnhPhongVu" nằm ở ngay gốc dự án của bạn
    private static final Path UPLOAD_DIR = Paths.get("uploads/HinhAnhPhongVu");

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
    @Autowired private ChiTietHoaDonRepository chiTietHoaDonRepository;
    @Autowired private ChucVuRepository chucVuRepository;
    @Autowired private MoTaRepository moTaRepository;
    @Autowired private GioHangRepository gioHangRepository;
    @Autowired private KhuyenMaiRepository khuyenMaiRepository;
    @Autowired private PhieuNhapHangRepository phieuNhapHangRepository;

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

    @GetMapping("/thongke-sanpham")
    public String thongKeSanPham(
            @RequestParam(value = "thang", required = false) Integer thang,
            @RequestParam(value = "nam", required = false) Integer nam,
            Model model) {

        int currentMonth = (thang != null) ? thang : LocalDate.now().getMonthValue();
        int currentYear = (nam != null) ? nam : LocalDate.now().getYear();

        List<HoaDon> hoaDons = hoaDonRepository.findByThangAndNam(currentMonth, currentYear);
        Map<String, ThongKeSanPhamItem> thongKeTheoSanPham = new LinkedHashMap<>();

        for (HoaDon hoaDon : hoaDons) {
            if (laHoaDonDaHuy(hoaDon)) {
                continue;
            }

            List<ChiTietHoaDon> chiTietHoaDons = chiTietHoaDonRepository.findByMaHD(hoaDon.getMaHD());
            for (ChiTietHoaDon chiTiet : chiTietHoaDons) {
                SanPham sanPham = chiTiet.getSanPham();
                if (sanPham == null && chiTiet.getMaSP() != null) {
                    sanPham = sanPhamRepository.findById(chiTiet.getMaSP()).orElse(null);
                }
                if (sanPham == null) {
                    continue;
                }

                SanPham sanPhamThongKe = sanPham;
                ThongKeSanPhamItem item = thongKeTheoSanPham.computeIfAbsent(
                        sanPhamThongKe.getMaSP(),
                        maSP -> new ThongKeSanPhamItem(sanPhamThongKe)
                );
                item.congSoLuong(parseSoLuong(chiTiet.getSoLuongSP_HD()));
                item.congThanhTien(chiTiet.getThanhTien());
            }
        }

        List<ThongKeSanPhamItem> danhSachSP = new ArrayList<>(thongKeTheoSanPham.values());
        danhSachSP.sort(Comparator.comparing(ThongKeSanPhamItem::getThanhTien).reversed());

        int tongSoLuongDaBan = danhSachSP.stream()
                .mapToInt(ThongKeSanPhamItem::getSoLuongBan)
                .sum();
        BigDecimal tongDoanhThuSanPham = danhSachSP.stream()
                .map(ThongKeSanPhamItem::getThanhTien)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("Thang", currentMonth);
        model.addAttribute("Nam", currentYear);
        model.addAttribute("TongSoLuongDaBan", tongSoLuongDaBan);
        model.addAttribute("TongDoanhThuSanPham", tongDoanhThuSanPham);
        model.addAttribute("DanhSachSP", danhSachSP);
        model.addAttribute("ChartLabels", danhSachSP.stream().limit(8).map(item -> item.getSanPham().getTenSP()).toList());
        model.addAttribute("ChartSoLuong", danhSachSP.stream().limit(8).map(ThongKeSanPhamItem::getSoLuongBan).toList());
        model.addAttribute("ChartDoanhThu", danhSachSP.stream().limit(8).map(item -> item.getThanhTien().longValue()).toList());
        model.addAttribute("TonKhoLabels", danhSachSP.stream().limit(8).map(item -> item.getSanPham().getTenSP()).toList());
        model.addAttribute("TonKhoValues", danhSachSP.stream().limit(8).map(item -> item.getSanPham().getSoLuongTon() != null ? item.getSanPham().getSoLuongTon() : 0).toList());

        return "Admin/ThongKeSanPham";
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
        List<HoaDon> listDH = hoaDonRepository.findAllByOrderByNgayLapDesc();
        model.addAttribute("listDH", listDH);
        model.addAttribute("KhuyenMaiTheoHD", taoMapKhuyenMaiTheoHoaDon(listDH));
        model.addAttribute("TienGiamTheoHD", taoMapTienGiamTheoHoaDon(listDH));
        return "Admin/QL_DonHang";
    }

    // QUAN LY DANH SACH KHUYEN MAI
    @GetMapping("/ql-khuyenmai")
    public String quanLyKhuyenMai(Model model) {
        model.addAttribute("QLKM", khuyenMaiRepository.findAllForQuanLy());
        return "Admin/AD_QLKM";
    }

    @GetMapping("/chitiet-donhang/{id}")
    public String chiTietDonHangAdmin(
            @PathVariable("id") String id,
            Model model,
            RedirectAttributes redirectAttributes) {

        HoaDon donHang = hoaDonRepository.findById(id).orElse(null);

        if (donHang == null) {
            redirectAttributes.addFlashAttribute("Error", "Khong tim thay don hang!");
            return "redirect:/admin/ql-donhang";
        }

        List<ChiTietHoaDon> chiTietHoaDons = chiTietHoaDonRepository.findByMaHD(id);
        donHang.setChiTietHoaDons(chiTietHoaDons);

        model.addAttribute("donHang", donHang);
        themThongTinKhuyenMaiHoaDon(model, donHang, chiTietHoaDons);
        return "Admin/ChiTietDonHang";
    }

    @PostMapping("/capnhat-trangthai")
    public String capNhatTrangThaiDonHang(
            @RequestParam("maDonHang") String maDonHang,
            @RequestParam("newStatus") String newStatus,
            @RequestParam(value = "returnToDetail", defaultValue = "false") boolean returnToDetail,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        HoaDon donHang = hoaDonRepository.findById(maDonHang).orElse(null);

        if (donHang == null) {
            redirectAttributes.addFlashAttribute("Error", "Khong tim thay don hang!");
            return "redirect:/admin/ql-donhang";
        }

        String maTK = (String) session.getAttribute("UserID");
        if (maTK != null && (donHang.getMaNV() == null || donHang.getMaNV().trim().isEmpty())) {
            TaiKhoan taiKhoan = taiKhoanRepository.findById(maTK).orElse(null);
            if (taiKhoan != null && taiKhoan.getMaNV() != null && !taiKhoan.getMaNV().trim().isEmpty()) {
                donHang.setMaNV(taiKhoan.getMaNV());
            }
        }

        String trangThaiCu = donHang.getTrangThaiTT();
        if (laTrangThaiKetThuc(trangThaiCu)) {
            redirectAttributes.addFlashAttribute("Error", "Don hang da o trang thai ket thuc, khong the cap nhat tiep.");
            return returnToDetail ? "redirect:/admin/chitiet-donhang/" + maDonHang : "redirect:/admin/ql-donhang";
        }

        if ("Đã hủy".equals(newStatus) && laTrangThaiDangGiao(trangThaiCu)) {
            redirectAttributes.addFlashAttribute("Error", "Don hang da giao/đang giao, khong the chuyen sang huy. Hay chon 'Khong nhan hang' neu khach khong nhan.");
            return returnToDetail ? "redirect:/admin/chitiet-donhang/" + maDonHang : "redirect:/admin/ql-donhang";
        }

        String thongBao = "Cap nhat trang thai don hang thanh cong!";
        if ("Không nhận hàng".equals(newStatus)) {
            hoanTraTonKhoTheoHoaDon(maDonHang);
            thongBao = "Da cap nhat khong nhan hang va tra lai so luong ton kho. Don hang nay khong hoan tien.";
        } else if ("Đã hủy".equals(newStatus)) {
            hoanTraTonKhoTheoHoaDon(maDonHang);
            thongBao = "Da huy don hang va tra lai so luong ton kho.";
            if ("Đã thanh toán".equals(trangThaiCu)) {
                thongBao += " Don hang da thanh toan truoc do nen can hoan tien cho khach.";
            }
        }

        donHang.setTrangThaiTT(newStatus);
        hoaDonRepository.save(donHang);

        redirectAttributes.addFlashAttribute("Success", thongBao);
        if (returnToDetail) {
            return "redirect:/admin/chitiet-donhang/" + maDonHang;
        }
        return "redirect:/admin/ql-donhang";
    }

    // 7. QUẢN LÝ DANH SÁCH TÀI KHOẢN
    @GetMapping("/ql-taikhoan")
    public String quanLyTaiKhoan(Model model) {
        model.addAttribute("QLTK", taiKhoanRepository.findAll());
        return "Admin/AD_QLTK";
    }

    @GetMapping("/create-km")
    public String createKhuyenMaiView(Model model) {
        model.addAttribute("khuyenMai", new KhuyenMai());
        model.addAttribute("dsHoaDon", hoaDonRepository.findAll());
        return "Admin/Create_KM";
    }

    @PostMapping("/create-km")
    public String createKhuyenMaiPost(@ModelAttribute KhuyenMai khuyenMai, RedirectAttributes redirectAttributes) {
        try {
            chuanHoaKhuyenMai(khuyenMai);
            if (!coGiaTri(khuyenMai.getMaKM())) {
                khuyenMai.setMaKM(generateMaKM());
            }
            if (!ngayKhuyenMaiHopLe(khuyenMai)) {
                redirectAttributes.addFlashAttribute("Error", "Ngay bat dau phai nho hon hoac bang ngay ket thuc.");
                return "redirect:/admin/create-km";
            }

            khuyenMaiRepository.save(khuyenMai);
            redirectAttributes.addFlashAttribute("Success", "Them khuyen mai thanh cong!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("Error", "Loi them khuyen mai: " + e.getMessage());
        }
        return "redirect:/admin/ql-khuyenmai";
    }

    @GetMapping("/edit-km/{id}")
    public String editKhuyenMaiView(@PathVariable("id") String id, Model model, RedirectAttributes redirectAttributes) {
        KhuyenMai khuyenMai = khuyenMaiRepository.findById(id).orElse(null);
        if (khuyenMai == null) {
            redirectAttributes.addFlashAttribute("Error", "Khong tim thay khuyen mai!");
            return "redirect:/admin/ql-khuyenmai";
        }

        model.addAttribute("khuyenMai", khuyenMai);
        model.addAttribute("dsHoaDon", hoaDonRepository.findAll());
        return "Admin/Edit_KM";
    }

    @PostMapping("/edit-km")
    public String editKhuyenMaiPost(@ModelAttribute KhuyenMai khuyenMaiMoi, RedirectAttributes redirectAttributes) {
        try {
            KhuyenMai khuyenMaiCu = khuyenMaiRepository.findById(khuyenMaiMoi.getMaKM()).orElse(null);
            if (khuyenMaiCu == null) {
                redirectAttributes.addFlashAttribute("Error", "Khong tim thay khuyen mai!");
                return "redirect:/admin/ql-khuyenmai";
            }

            chuanHoaKhuyenMai(khuyenMaiMoi);
            if (!ngayKhuyenMaiHopLe(khuyenMaiMoi)) {
                redirectAttributes.addFlashAttribute("Error", "Ngay bat dau phai nho hon hoac bang ngay ket thuc.");
                return "redirect:/admin/edit-km/" + khuyenMaiMoi.getMaKM();
            }

            khuyenMaiCu.setMaHD(khuyenMaiMoi.getMaHD());
            khuyenMaiCu.setTenKM(khuyenMaiMoi.getTenKM());
            khuyenMaiCu.setPhanTramKM(khuyenMaiMoi.getPhanTramKM());
            khuyenMaiCu.setSoTienToiDaKM(khuyenMaiMoi.getSoTienToiDaKM());
            khuyenMaiCu.setSoTienToiThieuNhanKM(khuyenMaiMoi.getSoTienToiThieuNhanKM());
            khuyenMaiCu.setNgayBD(khuyenMaiMoi.getNgayBD());
            khuyenMaiCu.setNgayKT(khuyenMaiMoi.getNgayKT());

            khuyenMaiRepository.save(khuyenMaiCu);
            redirectAttributes.addFlashAttribute("Success", "Cap nhat khuyen mai thanh cong!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("Error", "Loi cap nhat khuyen mai: " + e.getMessage());
        }
        return "redirect:/admin/ql-khuyenmai";
    }

    @PostMapping("/xoa-khuyenmai/{id}")
    public String xoaKhuyenMai(@PathVariable("id") String id, RedirectAttributes redirectAttributes) {
        try {
            khuyenMaiRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("Success", "Xoa khuyen mai thanh cong!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("Error", "Khuyen mai nay dang duoc lien ket voi hoa don hoac san pham, khong the xoa.");
        }
        return "redirect:/admin/ql-khuyenmai";
    }

    // ==========================================
    // THÊM SẢN PHẨM TRỰC TIẾP TỪ NÚT "THÊM MỚI"
    // ==========================================
    @GetMapping("/create-sp")
    public String createSanPhamView(Model model) {
        model.addAttribute("dsLoai", loaiSanPhamRepository.findAll());
        model.addAttribute("dsNSX", nhaSanXuatRepository.findAll());
        model.addAttribute("dsNCC", nhaCungCapRepository.findAll());
        return "Admin/Create_SP";
    }

    @PostMapping("/create-sp")
    @Transactional
    public String createSanPhamPost(
            @ModelAttribute SanPham sanPham,
            @ModelAttribute MoTa moTa,
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
            luuMoTaNeuCoDuLieu(sanPham, moTa);
            luuAnhSanPham(sanPham, fileAnhs);

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

        model.addAttribute("sanPham", sp);
        model.addAttribute("moTa", layMoTaDauTien(sp.getMaSP()));
        model.addAttribute("dsLoai", loaiSanPhamRepository.findAll());
        model.addAttribute("dsNSX", nhaSanXuatRepository.findAll());
        model.addAttribute("dsNCC", nhaCungCapRepository.findAll());

        return "Admin/Edit_SP";
    }

    @PostMapping("/edit-sp")
    @Transactional
    public String editSanPhamPost(
            @ModelAttribute SanPham sanPhamMoi,
            @ModelAttribute MoTa moTa,
            @RequestParam(value = "fileAnhs", required = false) MultipartFile[] fileAnhs,
            RedirectAttributes redirectAttributes) {
        try {
            SanPham spCu = sanPhamRepository.findById(sanPhamMoi.getMaSP()).orElse(null);
            if(spCu != null) {
                spCu.setTenSP(sanPhamMoi.getTenSP());
                spCu.setDonGiaSP(sanPhamMoi.getDonGiaSP());
                spCu.setSoLuongTon(sanPhamMoi.getSoLuongTon());
                spCu.setDonVT(sanPhamMoi.getDonVT());
                spCu.setMaLoai(sanPhamMoi.getMaLoai());
                spCu.setMaNCC(sanPhamMoi.getMaNCC());
                spCu.setMaNSX(sanPhamMoi.getMaNSX());

                sanPhamRepository.save(spCu);
                capNhatMoTa(spCu, moTa);

                if (coFileAnhMoi(fileAnhs)) {
                    xoaAnhSanPham(spCu.getMaSP());
                    luuAnhSanPham(spCu, fileAnhs);
                }
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
                khCu.setTenKH(khMoi.getTenKH());
                khCu.setSdtKH(khMoi.getSdtKH());
                khCu.setDiaChiKH(khMoi.getDiaChiKH());
                khCu.setEmailKH(khMoi.getEmailKH());

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
        model.addAttribute("dsChucVu", chucVuRepository.findAll());
        return "admin/Create_NV";
    }
    @PostMapping("/create-nv")
    public String createNhanVienPost(@ModelAttribute NhanVien nhanVien, RedirectAttributes redirectAttributes) {
        try {
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
        NhanVien nv = nhanVienRepository.findById(id).orElse(null);
        if (nv == null) {
            redirectAttributes.addFlashAttribute("Error", "Không tìm thấy nhân viên!");
            return "redirect:/admin/ql-nhanvien";
        }

        model.addAttribute("nhanVien", nv);
        model.addAttribute("dsChucVu", chucVuRepository.findAll());
        return "admin/Edit_NV";
    }
    @PostMapping("/edit-nv")
    public String editNhanVienPost(@ModelAttribute NhanVien nvMoi, RedirectAttributes redirectAttributes) {
        try {
            NhanVien nvCu = nhanVienRepository.findById(nvMoi.getMaNV()).orElse(null);
            if(nvCu != null) {
                nvCu.setTenNV(nvMoi.getTenNV());
                nvCu.setSdtNV(nvMoi.getSdtNV());
                nvCu.setDiaChiNV(nvMoi.getDiaChiNV());
                nvCu.setEmailNV(nvMoi.getEmailNV());
                nvCu.setMaCV(nvMoi.getMaCV());

                nhanVienRepository.save(nvCu);
                redirectAttributes.addFlashAttribute("Success", "Cập nhật nhân viên thành công!");
            } else {
                redirectAttributes.addFlashAttribute("Error", "Lỗi: Không tìm thấy nhân viên trong Database!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("Error", "Lỗi cập nhật: " + e.getMessage());
        }
        return "redirect:/admin/ql-nhanvien";
    }

    @PostMapping("/xoa-nhanvien/{id}")
    public String xoaNhanVien(@PathVariable("id") String id, RedirectAttributes redirectAttributes) {
        try {
            nhanVienRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("Success", "Xóa nhân viên thành công!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("Error", "Lỗi Khóa Ngoại! Nhân viên này đã liên kết với các dữ liệu khác trong hệ thống, không thể xóa.");
        }
        return "redirect:/admin/ql-nhanvien";
    }

    // ==========================================
    // THÊM TÀI KHOẢN MỚI
    // ==========================================
    @GetMapping("/tao-tk")
    public String createTaiKhoanView(Model model) {
        model.addAttribute("taiKhoan", new TaiKhoan());
        model.addAttribute("dsNhanVien", nhanVienRepository.findAll());
        model.addAttribute("dsKhachHang", khachHangRepository.findAll());
        return "admin/Create_TK";
    }

    @PostMapping("/tao-tk")
    public String createTaiKhoanPost(@ModelAttribute TaiKhoan taiKhoan, RedirectAttributes redirectAttributes) {
        try {
            if (taiKhoan.getMaNV() != null && taiKhoan.getMaNV().isEmpty()) taiKhoan.setMaNV(null);
            if (taiKhoan.getMaKH() != null && taiKhoan.getMaKH().isEmpty()) taiKhoan.setMaKH(null);
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
    @GetMapping("/sua-taikhoan/{id}")
    public String editTaiKhoanView(@PathVariable("id") String id, Model model, RedirectAttributes redirectAttributes) {
        TaiKhoan tk = taiKhoanRepository.findById(id).orElse(null);
        if (tk == null) {
            redirectAttributes.addFlashAttribute("Error", "Không tìm thấy tài khoản!");
            return "redirect:/admin/ql-taikhoan";
        }

        model.addAttribute("taiKhoan", tk);
        model.addAttribute("dsNhanVien", nhanVienRepository.findAll());
        model.addAttribute("dsKhachHang", khachHangRepository.findAll());
        return "admin/Edit_TK";
    }

    @PostMapping("/sua-tk")
    public String editTaiKhoanPost(@ModelAttribute TaiKhoan tkMoi, RedirectAttributes redirectAttributes) {
        try {
            TaiKhoan tkCu = taiKhoanRepository.findById(tkMoi.getMaTK()).orElse(null);
            if(tkCu != null) {
                tkCu.setEmailTK(tkMoi.getEmailTK());
                tkCu.setLoaiTaiKhoan(tkMoi.getLoaiTaiKhoan());

                String maNV = tkMoi.getMaNV();
                tkCu.setMaNV((maNV == null || maNV.trim().isEmpty()) ? null : maNV.trim());

                String maKH = tkMoi.getMaKH();
                tkCu.setMaKH((maKH == null || maKH.trim().isEmpty()) ? null : maKH.trim());

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

    @PostMapping("/doi-loai-taikhoan")
    public String doiLoaiTaiKhoan(@RequestParam("matk") String matk,
                                  @RequestParam("loaiMoi") String loaiMoi,
                                  RedirectAttributes redirectAttributes) {
        try {
            TaiKhoan tk = taiKhoanRepository.findById(matk).orElse(null);
            if (tk != null) {
                tk.setLoaiTaiKhoan(loaiMoi);
                taiKhoanRepository.save(tk);
                redirectAttributes.addFlashAttribute("Success", "Đã thay đổi phân quyền tài khoản thành công!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("Error", "Lỗi đổi quyền: " + e.getMessage());
        }
        return "redirect:/admin/ql-taikhoan";
    }

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

        model.addAttribute("dsLoai", loaiSanPhamRepository.findAll());
        model.addAttribute("dsNSX", nhaSanXuatRepository.findAll());
        model.addAttribute("dsNCC", nhaCungCapRepository.findAll());

        List<SanPham> dsSanPham = sanPhamRepository.findAll();
        model.addAttribute("dsSanPhamHienCo", dsSanPham);
        model.addAttribute("dsMoTaHienCo", moTaRepository.findAll());

        // --- ĐOẠN SỬA: Gom tên ảnh thực tế theo từng Mã Sản Phẩm ---
        Map<String, String> mapAnhSanPham = new HashMap<>();
        for (SanPham sp : dsSanPham) {
            // Tìm danh sách ảnh của sản phẩm này trong DB
            var dsAnh = danhSachAnhRepository.findBySanPham_MaSP(sp.getMaSP());
            if (dsAnh != null && !dsAnh.isEmpty()) {
                // Lấy cái ảnh đầu tiên của sản phẩm đó
                mapAnhSanPham.put(sp.getMaSP(), dsAnh.get(0).getTenAnh());
            } else {
                mapAnhSanPham.put(sp.getMaSP(), ""); // Không có ảnh
            }
        }
        model.addAttribute("mapAnhSanPham", mapAnhSanPham);
        // -----------------------------------------------------------

        return "admin/NhapHang";
    }

    // Tự động tạo mã Nhà Cung Cấp mới tăng dần (Ví dụ: NCC001, NCC002...)
    private synchronized String generateMaNCC() {
        List<NhaCungCap> dsNcc = nhaCungCapRepository.findAll();
        int max = 0;
        for (NhaCungCap ncc : dsNcc) {
            try {
                int num = Integer.parseInt(ncc.getMaNCC().substring(3));
                if (num > max) {
                    max = num;
                }
            } catch (Exception e) {}
        }
        return String.format("NCC%03d", max + 1);
    }

    // Tự động tạo mã Phiếu Nhập Hàng tăng dần (PNH001, PNH002...)
    private synchronized String generateMaPNH() {
        PhieuNhapHang last = phieuNhapHangRepository.findTopByOrderByMaPNHDesc();
        if (last == null || last.getMaPNH() == null) {
            return "PNH001";
        }
        try {
            int next = Integer.parseInt(last.getMaPNH().substring(3)) + 1;
            return String.format("PNH%03d", next);
        } catch (Exception e) {
            return "PNH" + System.currentTimeMillis() % 1000000;
        }
    }

    // CÁCH 1: XỬ LÝ NHẬP THỦ CÔNG & THÊM NCC MỚI NẾU TÍCH CHỌN

    @PostMapping("/nhap-thu-cong")
    @Transactional
    public String xuLyNhapThuCong(@ModelAttribute SanPham sp,
                                  @ModelAttribute MoTa mt,
                                  @RequestParam("fileAnh") MultipartFile fileAnh,
                                  // --- THÊM 2 THAM SỐ NÀY ĐỂ NHẬN BIẾT SẢN PHẨM CŨ ---
                                  @RequestParam(value = "isOldSP", defaultValue = "false") boolean isOldSP,
                                  @RequestParam(value = "maSPDaCo", required = false) String maSPDaCo,
                                  // ----------------------------------------------------
                                  @RequestParam(value = "isNewNCC", defaultValue = "false") boolean isNewNCC,
                                  @RequestParam(value = "tenNCCMoi", required = false) String tenNCCMoi,
                                  @RequestParam(value = "sdtNCCMoi", required = false) String sdtNCCMoi,
                                  @RequestParam(value = "diaChiNCCMoi", required = false) String diaChiNCCMoi,
                                  @RequestParam(value = "thueVAT", defaultValue = "10") BigDecimal thueVATPct,
                                  @RequestParam(value = "chietKhau", defaultValue = "0") BigDecimal chietKhauPct,
                                  RedirectAttributes redirectAttributes) {
        try {
            // TRƯỜNG HỢP 1: NẾU NGƯỜI DÙNG CHỌN SẢN PHẨM ĐÃ CÓ SẴN (CỘNG DỒN SỐ LƯỢNG)
            if (isOldSP && maSPDaCo != null && !maSPDaCo.trim().isEmpty()) {
                SanPham spHienTai = sanPhamRepository.findById(maSPDaCo)
                        .orElseThrow(() -> new Exception("Không tìm thấy sản phẩm trong kho!"));

                // Cập nhật giá bán mới nếu người dùng điền giá mới trên form
                if (sp.getDonGiaSP() != null) {
                    spHienTai.setDonGiaSP(sp.getDonGiaSP());
                }

                // Cộng dồn số lượng nhập thêm vào số lượng đang có sẵn trong DB
                int soLuongNhapThem = (sp.getSoLuongTon() != null) ? sp.getSoLuongTon() : 0;
                int soLuongCu = (spHienTai.getSoLuongTon() != null) ? spHienTai.getSoLuongTon() : 0;
                spHienTai.setSoLuongTon(soLuongCu + soLuongNhapThem);

                // Lưu cập nhật sản phẩm cũ vào database
                sanPhamRepository.save(spHienTai);

                // --- ĐOẠN XỬ LÝ UPLOAD ẢNH CHO SẢN PHẨM CŨ ---
                // Nếu người dùng có chọn file ảnh mới khi nhập kho mặt hàng cũ này
                if (fileAnh != null && !fileAnh.isEmpty()) {
                    // Tạo thư mục ngoài nếu chưa tồn tại
                    Files.createDirectories(UPLOAD_DIR);

                    // Đếm xem sản phẩm này hiện đã có bao nhiêu ảnh trong DB để tăng số index sau đuôi ảnh
                    int index = danhSachAnhRepository.findBySanPham_MaSP(spHienTai.getMaSP()).size() + 1;

                    // Quy chuẩn đặt tên file ảnh (Ví dụ: SP001_02.jpg)
                    String tenAnhMoi = spHienTai.getMaSP() + "_" + String.format("%02d", index) + layDuoiFile(fileAnh.getOriginalFilename());
                    Path filePath = UPLOAD_DIR.resolve(tenAnhMoi);

                    // Ghi file vật lý ra thư mục ngoài uploads/HinhAnhPhongVu
                    Files.write(filePath, fileAnh.getBytes());

                    // Lưu thông tin bản ghi vào bảng DANHSACHANH trong DB
                    DanhSachAnh anhEntity = new DanhSachAnh();
                    anhEntity.setMaDsa(generateMaDSA());
                    anhEntity.setSanPham(spHienTai);
                    anhEntity.setTenAnh(tenAnhMoi);
                    danhSachAnhRepository.save(anhEntity);
                }
                // ----------------------------------------------

                // Đồng bộ lại thông tin của đối tượng 'sp' để đoạn code tính tiền phiếu nhập bên dưới chạy chính xác
                sp.setMaNCC(spHienTai.getMaNCC());
                sp.setDonGiaSP(spHienTai.getDonGiaSP());
                sp.setSoLuongTon(soLuongNhapThem); // Chỉ lấy số lượng mới nhập để tính tiền phiếu nhập

            } else {
                // TRƯỜNG HỢP 2: THÊM MỚI TOÀN BỘ (ĐÂY LÀ CODE CŨ CỦA BẠN)
                if (isNewNCC && tenNCCMoi != null && !tenNCCMoi.trim().isEmpty()) {
                    NhaCungCap nccMoi = new NhaCungCap();
                    String newMaNCC = generateMaNCC();

                    nccMoi.setMaNCC(newMaNCC);
                    nccMoi.setTenNCC(tenNCCMoi.trim());
                    nccMoi.setSdtNCC(sdtNCCMoi != null ? sdtNCCMoi.trim() : "");
                    nccMoi.setDiaChiNCC(diaChiNCCMoi != null ? diaChiNCCMoi.trim() : "");

                    nhaCungCapRepository.save(nccMoi);
                    sp.setMaNCC(newMaNCC);
                }

                // Sửa lại đoạn upload ảnh hàng mới thọc thẳng ra folder "uploads/" vật lý ngoài máy tính
                String maSPMoi = adminService.generateMaSP();
                sp.setMaSP(maSPMoi);
                sp.setNgayCN(LocalDateTime.now());
                sanPhamRepository.save(sp);

                // Lưu mô tả cấu hình sản phẩm mới
                luuMoTaNeuCoDuLieu(sp, mt);

                // Xử lý upload ảnh cho sản phẩm mới vào folder vật lý ngoài
                if (fileAnh != null && !fileAnh.isEmpty()) {
                    Files.createDirectories(UPLOAD_DIR);
                    String tenAnhMoi = maSPMoi + "_01" + layDuoiFile(fileAnh.getOriginalFilename());
                    Path filePath = UPLOAD_DIR.resolve(tenAnhMoi);
                    Files.write(filePath, fileAnh.getBytes());

                    DanhSachAnh anhEntity = new DanhSachAnh();
                    anhEntity.setMaDsa(generateMaDSA());
                    anhEntity.setSanPham(sp);
                    anhEntity.setTenAnh(tenAnhMoi);
                    danhSachAnhRepository.save(anhEntity);
                }
            }

            // ===== TẠO PHIẾU NHẬP HÀNG TỰ ĐỘNG (ÁP DỤNG CHO CẢ HÀNG MỚI LẪN HÀNG CŨ) =====
            String maNCC = sp.getMaNCC();
            if (maNCC != null && !maNCC.trim().isEmpty()) {
                LocalDateTime now = LocalDateTime.now();

                // Tính tổng tiền hàng = giá × số lượng
                BigDecimal tongHang = BigDecimal.ZERO;
                if (sp.getDonGiaSP() != null && sp.getSoLuongTon() != null) {
                    tongHang = sp.getDonGiaSP()
                            .multiply(BigDecimal.valueOf(sp.getSoLuongTon()));
                }

                // Tính tiền VAT và chiết khấu từ %
                BigDecimal tienVAT   = tongHang.multiply(thueVATPct)
                        .divide(BigDecimal.valueOf(100));
                BigDecimal tienCK    = tongHang.multiply(chietKhauPct)
                        .divide(BigDecimal.valueOf(100));
                BigDecimal tongCong  = tongHang.add(tienVAT).subtract(tienCK);

                PhieuNhapHang phieu = new PhieuNhapHang();
                phieu.setMaPNH(generateMaPNH());
                phieu.setMaNCC(maNCC.trim());
                phieu.setNgayGiao(now);
                phieu.setNgayNhan(now);
                phieu.setTrangThaiThanhToan("Chưa thanh toán");
                phieu.setThueVAT(tienVAT);
                phieu.setChietKhau(tienCK);
                phieu.setTongCong(tongCong.toPlainString());

                phieuNhapHangRepository.save(phieu);
            }
            // ============================================================================

            redirectAttributes.addFlashAttribute("Success",
                    "Nhập hàng thành công! Dữ liệu kho, hình ảnh và phiếu nhập đã được đồng bộ thời gian thực.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("Error", "Lỗi nhập hàng: " + e.getMessage());
        }
        return "redirect:/admin/ql-phieunhap";
    }

    // 8B. QUẢN LÝ PHIẾU NHẬP HÀNG
    @GetMapping("/ql-phieunhap")
    public String quanLyPhieuNhap(Model model) {
        // Dùng findAll() thường, sau đó sort trong Java để tránh lỗi Sort với một số repo
        List<PhieuNhapHang> listPNH = phieuNhapHangRepository.findAll();
        listPNH.sort((a, b) -> {
            if (a.getMaPNH() == null) return 1;
            if (b.getMaPNH() == null) return -1;
            return b.getMaPNH().compareTo(a.getMaPNH()); // DESC
        });

        // Map maNCC -> tenNCC để hiển thị tên thay vì mã
        Map<String, String> tenNCC = new HashMap<>();
        try {
            nhaCungCapRepository.findAll().forEach(ncc -> {
                if (ncc.getMaNCC() != null && ncc.getTenNCC() != null) {
                    tenNCC.put(ncc.getMaNCC(), ncc.getTenNCC());
                }
            });
        } catch (Exception ignored) {}

        model.addAttribute("listPNH", listPNH);
        model.addAttribute("TenNCC", tenNCC);
        return "admin/QL_PhieuNhapHang";
    }

    @PostMapping("/capnhat-trangthai-phieunhap")
    public String capNhatTrangThaiPhieuNhap(
            @RequestParam("maPNH") String maPNH,
            @RequestParam("newStatus") String newStatus,
            RedirectAttributes redirectAttributes) {
        try {
            PhieuNhapHang phieu = phieuNhapHangRepository.findById(maPNH).orElse(null);
            if (phieu == null) {
                redirectAttributes.addFlashAttribute("Error", "Không tìm thấy phiếu: " + maPNH);
            } else {
                phieu.setTrangThaiThanhToan(newStatus);
                phieuNhapHangRepository.save(phieu);
                redirectAttributes.addFlashAttribute("Success",
                        "Cập nhật trạng thái phiếu " + maPNH + " thành công!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("Error", "Lỗi cập nhật: " + e.getMessage());
        }
        return "redirect:ql-phieunhap";
    }

    @PostMapping("/xoa-phieunhap/{id}")
    public String xoaPhieuNhap(@PathVariable("id") String maPNH,
                               RedirectAttributes redirectAttributes) {
        try {
            if (!phieuNhapHangRepository.existsById(maPNH)) {
                redirectAttributes.addFlashAttribute("Error", "Không tìm thấy phiếu: " + maPNH);
                return "redirect:/admin/ql-phieunhap";
            }

            // Kiểm tra còn sản phẩm nào đang tham chiếu tới phiếu này không
            long soSP = sanPhamRepository.findAll().stream()
                    .filter(sp -> maPNH.equals(sp.getMaPNH()))
                    .count();

            if (soSP > 0) {
                redirectAttributes.addFlashAttribute("Error",
                        "Không thể xóa phiếu " + maPNH + " vì còn " + soSP
                                + " sản phẩm đang thuộc phiếu này!");
                return "redirect:/admin/ql-phieunhap";
            }

            phieuNhapHangRepository.deleteById(maPNH);
            redirectAttributes.addFlashAttribute("Success",
                    "Đã xóa phiếu " + maPNH + " thành công!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("Error", "Lỗi xóa phiếu: " + e.getMessage());
        }
        return "redirect:/admin/ql-phieunhap";
    }

    // 9. THAO TÁC XÓA SẢN PHẨM (HỖ TRỢ BẮT LỖI KHÓA NGOẠI)
    @PostMapping("/xoa-sanpham/{id}")
    @Transactional
    public String xoaSanPham(@PathVariable("id") String id, RedirectAttributes redirectAttributes) {
        try {
            xoaSanPhamVaDuLieuLienQuan(id, redirectAttributes);
            redirectAttributes.addFlashAttribute("Success", "Xóa sản phẩm thành công!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("Error", "Lỗi Khóa Ngoại! Sản phẩm này đã tồn tại trong Chi Tiết Đơn Hàng!");
        }
        return "redirect:/admin/ql-sanpham";
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
    @Transactional
    public String deleteSanPhamPost(
            @RequestParam("maSP") String maSP,
            RedirectAttributes redirectAttributes) {

        try {
            xoaSanPhamVaDuLieuLienQuan(maSP, redirectAttributes);
            redirectAttributes.addFlashAttribute("Success", "Xóa sản phẩm thành công!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute(
                    "Error",
                    "Không thể xóa sản phẩm này vì đã liên kết với hóa đơn hoặc dữ liệu khác."
            );
        }

        return "redirect:/admin/ql-sanpham";
    }

    private MoTa layMoTaDauTien(String maSP) {
        List<MoTa> moTas = moTaRepository.findByMaSP(maSP);
        if (moTas == null || moTas.isEmpty()) {
            return new MoTa();
        }
        return moTas.get(0);
    }

    private boolean laHoaDonDaHuy(HoaDon hoaDon) {
        if (hoaDon == null || hoaDon.getTrangThaiTT() == null) {
            return false;
        }

        String trangThai = hoaDon.getTrangThaiTT().toLowerCase();
        return trangThai.contains("huy")
                || trangThai.contains("hủy")
                || trangThai.contains("khong nhan")
                || trangThai.contains("không nhận");
    }

    private boolean laTrangThaiKetThuc(String trangThai) {
        if (trangThai == null) {
            return false;
        }

        return "Đã hủy".equals(trangThai)
                || "Đã nhận hàng".equals(trangThai)
                || "Không nhận hàng".equals(trangThai);
    }

    private boolean laTrangThaiDangGiao(String trangThai) {
        if (trangThai == null) {
            return false;
        }

        return "Đang giao".equals(trangThai) || "Đã giao".equals(trangThai);
    }

    private void hoanTraTonKhoTheoHoaDon(String maHD) {
        List<ChiTietHoaDon> chiTietHoaDons = chiTietHoaDonRepository.findByMaHD(maHD);
        for (ChiTietHoaDon chiTiet : chiTietHoaDons) {
            SanPham sanPham = sanPhamRepository.findById(chiTiet.getMaSP()).orElse(null);
            if (sanPham == null) {
                continue;
            }

            int soLuongTra = parseSoLuong(chiTiet.getSoLuongSP_HD());
            sanPham.setSoLuongTon((sanPham.getSoLuongTon() != null ? sanPham.getSoLuongTon() : 0) + soLuongTra);
            sanPhamRepository.save(sanPham);
        }
    }

    private int parseSoLuong(String soLuong) {
        if (soLuong == null || soLuong.trim().isEmpty()) {
            return 0;
        }

        try {
            return Integer.parseInt(soLuong.trim().replace(",", ""));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    public static class ThongKeSanPhamItem {
        private final SanPham sanPham;
        private int soLuongBan;
        private BigDecimal thanhTien = BigDecimal.ZERO;

        public ThongKeSanPhamItem(SanPham sanPham) {
            this.sanPham = sanPham;
        }

        public SanPham getSanPham() {
            return sanPham;
        }

        public int getSoLuongBan() {
            return soLuongBan;
        }

        public BigDecimal getThanhTien() {
            return thanhTien;
        }

        public void congSoLuong(int soLuong) {
            this.soLuongBan += soLuong;
        }

        public void congThanhTien(BigDecimal thanhTien) {
            if (thanhTien != null) {
                this.thanhTien = this.thanhTien.add(thanhTien);
            }
        }
    }

    private void luuMoTaNeuCoDuLieu(SanPham sanPham, MoTa moTa) {
        if (!coDuLieuMoTa(moTa)) {
            return;
        }

        moTa.setMaMT(generateMaMT());
        moTa.setMaSP(sanPham.getMaSP());
        chuanHoaMoTa(moTa);
        moTaRepository.save(moTa);
    }

    private void capNhatMoTa(SanPham sanPham, MoTa moTaMoi) {
        List<MoTa> moTasCu = moTaRepository.findByMaSP(sanPham.getMaSP());

        if (!coDuLieuMoTa(moTaMoi)) {
            if (moTasCu != null && !moTasCu.isEmpty()) {
                moTaRepository.deleteByMaSP(sanPham.getMaSP());
            }
            return;
        }

        MoTa moTaCanLuu = (moTasCu != null && !moTasCu.isEmpty()) ? moTasCu.get(0) : new MoTa();
        if (moTaCanLuu.getMaMT() == null || moTaCanLuu.getMaMT().trim().isEmpty()) {
            moTaCanLuu.setMaMT(generateMaMT());
        }
        moTaCanLuu.setMaSP(sanPham.getMaSP());
        moTaCanLuu.setRam(chuanHoaChuoi(moTaMoi.getRam()));
        moTaCanLuu.setCpu(chuanHoaChuoi(moTaMoi.getCpu()));
        moTaCanLuu.setRom(chuanHoaChuoi(moTaMoi.getRom()));
        moTaCanLuu.setManHinh(chuanHoaChuoi(moTaMoi.getManHinh()));
        moTaCanLuu.setVga(chuanHoaChuoi(moTaMoi.getVga()));
        moTaCanLuu.setKhac(chuanHoaChuoi(moTaMoi.getKhac()));
        moTaRepository.save(moTaCanLuu);
    }

    private boolean coDuLieuMoTa(MoTa moTa) {
        return moTa != null
                && (coGiaTri(moTa.getRam())
                || coGiaTri(moTa.getCpu())
                || coGiaTri(moTa.getRom())
                || coGiaTri(moTa.getManHinh())
                || coGiaTri(moTa.getVga())
                || coGiaTri(moTa.getKhac()));
    }

    private void chuanHoaMoTa(MoTa moTa) {
        moTa.setRam(chuanHoaChuoi(moTa.getRam()));
        moTa.setCpu(chuanHoaChuoi(moTa.getCpu()));
        moTa.setRom(chuanHoaChuoi(moTa.getRom()));
        moTa.setManHinh(chuanHoaChuoi(moTa.getManHinh()));
        moTa.setVga(chuanHoaChuoi(moTa.getVga()));
        moTa.setKhac(chuanHoaChuoi(moTa.getKhac()));
    }

    private boolean coFileAnhMoi(MultipartFile[] fileAnhs) {
        if (fileAnhs == null) {
            return false;
        }

        for (MultipartFile file : fileAnhs) {
            if (file != null && !file.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private void luuAnhSanPham(SanPham sanPham, MultipartFile[] fileAnhs) throws Exception {
        if (!coFileAnhMoi(fileAnhs)) {
            return;
        }

        Files.createDirectories(UPLOAD_DIR);
        int index = danhSachAnhRepository.findBySanPham_MaSP(sanPham.getMaSP()).size() + 1;

        for (MultipartFile file : fileAnhs) {
            if (file == null || file.isEmpty()) {
                continue;
            }

            String tenAnh = sanPham.getMaSP() + "_" + String.format("%02d", index) + layDuoiFile(file.getOriginalFilename());
            Path filePath = UPLOAD_DIR.resolve(tenAnh);
            Files.write(filePath, file.getBytes());

            DanhSachAnh anh = new DanhSachAnh();
            anh.setMaDsa(generateMaDSA());
            anh.setSanPham(sanPham);
            anh.setTenAnh(tenAnh);
            danhSachAnhRepository.save(anh);
            index++;
        }
    }

    private void xoaAnhSanPham(String maSP) throws Exception {
        List<DanhSachAnh> anhs = danhSachAnhRepository.findBySanPham_MaSP(maSP);
        danhSachAnhRepository.deleteBySanPham_MaSP(maSP);
        for (DanhSachAnh anh : anhs) {
            xoaFileAnh(anh.getTenAnh());
        }
    }

    private void xoaSanPhamVaDuLieuLienQuan(String maSP, RedirectAttributes redirectAttributes) throws Exception {
        if (chiTietHoaDonRepository.existsByMaSP(maSP)) {
            throw new IllegalStateException("San pham da co trong chi tiet hoa don, khong the xoa.");
        }

        List<DanhSachAnh> anhs = danhSachAnhRepository.findBySanPham_MaSP(maSP);
        gioHangRepository.deleteByMaSP(maSP);
        danhSachAnhRepository.deleteBySanPham_MaSP(maSP);
        moTaRepository.deleteByMaSP(maSP);
        sanPhamRepository.deleteById(maSP);

        for (DanhSachAnh anh : anhs) {
            xoaFileAnh(anh.getTenAnh());
        }

        redirectAttributes.addFlashAttribute("Success", "Xoa san pham thanh cong!");
    }

    private void xoaFileAnh(String tenAnh) throws Exception {
        if (!coGiaTri(tenAnh)) {
            return;
        }

        Path uploadDir = UPLOAD_DIR.toAbsolutePath().normalize();
        Path filePath = uploadDir.resolve(tenAnh).normalize();
        if (filePath.startsWith(uploadDir)) {
            Files.deleteIfExists(filePath);
        }
    }

    private boolean coGiaTri(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private Map<String, KhuyenMai> taoMapKhuyenMaiTheoHoaDon(List<HoaDon> hoaDons) {
        Map<String, KhuyenMai> result = new HashMap<>();
        for (HoaDon hoaDon : hoaDons) {
            if (hoaDon != null && hoaDon.getMaHD() != null) {
                khuyenMaiRepository.findByMaHD(hoaDon.getMaHD())
                        .ifPresent(khuyenMai -> result.put(hoaDon.getMaHD(), khuyenMai));
            }
        }
        return result;
    }

    private Map<String, BigDecimal> taoMapTienGiamTheoHoaDon(List<HoaDon> hoaDons) {
        Map<String, BigDecimal> result = new HashMap<>();
        for (HoaDon hoaDon : hoaDons) {
            if (hoaDon != null && hoaDon.getMaHD() != null) {
                BigDecimal tongTienHang = tinhTongTienHang(chiTietHoaDonRepository.findByMaHD(hoaDon.getMaHD()));
                result.put(hoaDon.getMaHD(), tinhTienGiam(tongTienHang, hoaDon.getTongTienHD()));
            }
        }
        return result;
    }

    private void themThongTinKhuyenMaiHoaDon(Model model, HoaDon hoaDon, List<ChiTietHoaDon> chiTietHoaDons) {
        BigDecimal tongTienHang = tinhTongTienHang(chiTietHoaDons);
        BigDecimal tienGiam = tinhTienGiam(tongTienHang, hoaDon != null ? hoaDon.getTongTienHD() : null);

        model.addAttribute("TongTienHang", tongTienHang);
        model.addAttribute("TienGiam", tienGiam);
        model.addAttribute("KhuyenMaiDonHang",
                hoaDon != null && hoaDon.getMaHD() != null
                        ? khuyenMaiRepository.findByMaHD(hoaDon.getMaHD()).orElse(null)
                        : null);
    }

    private BigDecimal tinhTongTienHang(List<ChiTietHoaDon> chiTietHoaDons) {
        BigDecimal tongTienHang = BigDecimal.ZERO;
        if (chiTietHoaDons == null) {
            return tongTienHang;
        }

        for (ChiTietHoaDon chiTiet : chiTietHoaDons) {
            if (chiTiet != null && chiTiet.getThanhTien() != null) {
                tongTienHang = tongTienHang.add(chiTiet.getThanhTien());
            }
        }
        return tongTienHang;
    }

    private BigDecimal tinhTienGiam(BigDecimal tongTienHang, BigDecimal tongTienHoaDon) {
        if (tongTienHang == null || tongTienHoaDon == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal tienGiam = tongTienHang.subtract(tongTienHoaDon);
        return tienGiam.compareTo(BigDecimal.ZERO) > 0 ? tienGiam : BigDecimal.ZERO;
    }

    private String chuanHoaChuoi(String value) {
        return coGiaTri(value) ? value.trim() : null;
    }

    private void chuanHoaKhuyenMai(KhuyenMai khuyenMai) {
        khuyenMai.setMaKM(chuanHoaChuoi(khuyenMai.getMaKM()));
        khuyenMai.setMaHD(chuanHoaChuoi(khuyenMai.getMaHD()));
        khuyenMai.setTenKM(chuanHoaChuoi(khuyenMai.getTenKM()));
        khuyenMai.setPhanTramKM(chuanHoaChuoi(khuyenMai.getPhanTramKM()));
    }

    private boolean ngayKhuyenMaiHopLe(KhuyenMai khuyenMai) {
        return khuyenMai.getNgayBD() == null
                || khuyenMai.getNgayKT() == null
                || !khuyenMai.getNgayBD().isAfter(khuyenMai.getNgayKT());
    }

    private synchronized String generateMaKM() {
        KhuyenMai last = khuyenMaiRepository.findTopByOrderByMaKMDesc();

        if (last == null || last.getMaKM() == null) {
            return "KM001";
        }

        try {
            int next = Integer.parseInt(last.getMaKM().substring(2)) + 1;
            return String.format("KM%03d", next);
        } catch (Exception e) {
            return "KM" + System.currentTimeMillis() % 1000000;
        }
    }

    private synchronized String generateMaMT() {
        MoTa last = moTaRepository.findTopByOrderByMaMTDesc();

        if (last == null || last.getMaMT() == null) {
            return "MT001";
        }

        try {
            int next = Integer.parseInt(last.getMaMT().substring(2)) + 1;
            return String.format("MT%03d", next);
        } catch (Exception e) {
            return "MT" + System.currentTimeMillis() % 1000000;
        }
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