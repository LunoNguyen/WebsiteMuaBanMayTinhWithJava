package com.nhom8.DoAnJava.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.nhom8.DoAnJava.model.SanPham;
import com.nhom8.DoAnJava.repository.LoaiSanPhamRepository;
import com.nhom8.DoAnJava.repository.NhaSanXuatRepository;
import com.nhom8.DoAnJava.repository.SanPhamRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {

    @Autowired
    private SanPhamRepository sanPhamRepository;

    // Nhớ thêm @Autowired cho 2 Repository này ở đầu class nếu chưa có
    @Autowired
    private NhaSanXuatRepository nhaSanXuatRepository;

    @Autowired
    private LoaiSanPhamRepository loaiSanPhamRepository;

    @GetMapping("/gioi-thieu")
    public String gioiThieu() {
        return "home/GioiThieu"; 
    }

    @GetMapping({"/", "/trang-chu"})
    public String trangChu(
            @RequestParam(name = "page", defaultValue = "1") int page,
            Model model
    ) {
        final int PAGE_SIZE = 15;

        if (page < 1) {
            page = 1;
        }

        Pageable pageable = PageRequest.of(
                page - 1,
                PAGE_SIZE,
                Sort.by(Sort.Direction.DESC, "maSP")
        );

        Page<SanPham> pageSanPham = sanPhamRepository.findAll(pageable);

        int totalPages = pageSanPham.getTotalPages();
        if (totalPages > 0 && page > totalPages) {
            page = totalPages;
            pageable = PageRequest.of(
                    page - 1,
                    PAGE_SIZE,
                    Sort.by(Sort.Direction.DESC, "maSP")
            );
            pageSanPham = sanPhamRepository.findAll(pageable);
            totalPages = pageSanPham.getTotalPages();
        }

        int maxPageDisplay = 10;
        int startPage = 1;
        int endPage = totalPages;

        if (totalPages > maxPageDisplay) {
            startPage = Math.max(1, page - 4);
            endPage = startPage + maxPageDisplay - 1;

            if (endPage > totalPages) {
                endPage = totalPages;
                startPage = endPage - maxPageDisplay + 1;
            }
        }

        model.addAttribute("dssp", pageSanPham.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", pageSanPham.getTotalElements());
        model.addAttribute("pageSize", PAGE_SIZE);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        model.addAttribute("dsnsx", nhaSanXuatRepository.findAll());
        model.addAttribute("dslsp", loaiSanPhamRepository.findAll());

        return "home/TrangChu";
    }


    @GetMapping("/chi-tiet-san-pham/{id}")
    public String chiTietSanPham(@PathVariable String id, Model model) {
        SanPham sp = sanPhamRepository.findById(id).orElse(null);

        if (sp == null) {
            return "redirect:/trang-chu";
        }

        List<SanPham> spCungNSX = sanPhamRepository.findByMaNSX(sp.getMaNSX())
                .stream()
                .filter(item -> !item.getMaSP().equals(sp.getMaSP()))
                .toList();

        List<SanPham> spCungLSP = sanPhamRepository.findByMaLoai(sp.getMaLoai())
                .stream()
                .filter(item -> !item.getMaSP().equals(sp.getMaSP()))
                .toList();

        model.addAttribute("sp", sp);
        model.addAttribute("SPcungNSX", spCungNSX);
        model.addAttribute("SPcungLSP", spCungLSP);

        return "home/ChiTietSanPham";
    }

    @PostMapping("/tim-kiem")
    public String xuLyTimKiem(@RequestParam("txtTuKhoa") String txtTuKhoa, Model model) {
        List<SanPham> dssp = sanPhamRepository.findByTenSPContainingAndSoLuongTonGreaterThanEqual(txtTuKhoa, 0);

        model.addAttribute("dssp", dssp);
        model.addAttribute("dsnsx", nhaSanXuatRepository.findAll());
        model.addAttribute("dslsp", loaiSanPhamRepository.findAll());

        return "home/TrangChu";
    }

    // Bắt sự kiện click vào Thương Hiệu (Nhà Sản Xuất)
    @GetMapping("/tim-kiem/nsx/{id}")
    public String timKiemTheoNSX(@PathVariable("id") String id, Model model) {
        // Lấy danh sách sản phẩm ĐÃ LỌC từ Database
        model.addAttribute("dssp", sanPhamRepository.findByMaNSX(id));
        
        // Vẫn phải bơm lại 2 cục danh mục này để giao diện không bị trống
        model.addAttribute("dsnsx", nhaSanXuatRepository.findAll());
        model.addAttribute("dslsp", loaiSanPhamRepository.findAll());
        
        return "home/TrangChu";
    }

    // Bắt sự kiện click vào Loại Sản Phẩm
    @GetMapping("/tim-kiem/loai/{id}")
    public String timKiemTheoLoai(@PathVariable("id") String id, Model model) {
        // Lấy danh sách sản phẩm ĐÃ LỌC từ Database
        model.addAttribute("dssp", sanPhamRepository.findByMaLoai(id));
        
        model.addAttribute("dsnsx", nhaSanXuatRepository.findAll());
        model.addAttribute("dslsp", loaiSanPhamRepository.findAll());
        
        return "home/TrangChu";
    }
}