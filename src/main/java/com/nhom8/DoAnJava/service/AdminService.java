package com.nhom8.DoAnJava.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.nhom8.DoAnJava.model.ChiTietHoaDon;
import com.nhom8.DoAnJava.model.HoaDon;
import com.nhom8.DoAnJava.model.MoTa;
import com.nhom8.DoAnJava.model.SanPham;
import com.nhom8.DoAnJava.repository.ChiTietHoaDonRepository;
import com.nhom8.DoAnJava.repository.HoaDonRepository;
import com.nhom8.DoAnJava.repository.LoaiSanPhamRepository;
import com.nhom8.DoAnJava.repository.NhaSanXuatRepository;
import com.nhom8.DoAnJava.repository.NhanVienRepository;
import com.nhom8.DoAnJava.repository.SanPhamRepository;
import com.nhom8.DoAnJava.repository.TaiKhoanRepository;

@Service
public class AdminService {

    @Autowired private SanPhamRepository sanPhamRepository;
    @Autowired private NhaSanXuatRepository nhaSanXuatRepository;
    @Autowired private LoaiSanPhamRepository loaiSanPhamRepository;
    // Giả định bạn đã tạo sẵn các Repository tương ứng dưới đây
    @Autowired private HoaDonRepository hoaDonRepository; 
    @Autowired private ChiTietHoaDonRepository chiTietHoaDonRepository;
    @Autowired private NhanVienRepository nhanVienRepository;
    @Autowired private TaiKhoanRepository taiKhoanRepository;

    // 1. TÍNH TOÁN DOANH THU & THỐNG KÊ DASHBOARD
    public Map<String, Object> getDashboardStats(int thang, int nam) {
        Map<String, Object> stats = new HashMap<>();
        
        List<HoaDon> hoaDons = hoaDonRepository.findByThangAndNam(thang, nam);
        
        // ĐỔI TÊN KEY CHO KHỚP VỚI GIAO DIỆN THYMELEAF (Viết hoa chữ cái đầu)
        stats.put("SoDonHang", hoaDons.size());

        BigDecimal doanhThu = hoaDons.stream()
                .map(HoaDon::getTongTienHD)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("TongDoanhThu", doanhThu);

        long soKhachHang = hoaDons.stream().map(HoaDon::getKhachHang).distinct().count();
        stats.put("SoKhachHang", soKhachHang);

        int soSPBanRa = 0;
        for (HoaDon hd : hoaDons) {
            List<ChiTietHoaDon> listChiTiet = chiTietHoaDonRepository.findByMaHD(hd.getMaHD());
            if (listChiTiet != null) {
                for (ChiTietHoaDon ct : listChiTiet) {
                    try {
                        soSPBanRa += Integer.parseInt(ct.getSoLuongSP_HD()); 
                    } catch (Exception e) {}
                }
            }
        }
        stats.put("SoSanPhamBanRa", soSPBanRa);
        
        // Thêm chốt chặn an toàn: Bỏ qua sắp xếp nếu ngày lập bị null
        hoaDons.sort((h1, h2) -> {
            if (h1.getNgayLap() == null || h2.getNgayLap() == null) return 0;
            return h2.getNgayLap().compareTo(h1.getNgayLap());
        });
        stats.put("DonHangMoi", hoaDons.stream().limit(5).toList());

        List<String> nhanDoanhThuTheoThang = new ArrayList<>();
        List<Long> doanhThuTheoThang = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            BigDecimal doanhThuThang = hoaDonRepository.findByThangAndNam(i, nam).stream()
                    .map(HoaDon::getTongTienHD)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            nhanDoanhThuTheoThang.add("Thang " + i);
            doanhThuTheoThang.add(doanhThuThang.longValue());
        }
        stats.put("NhanDoanhThuTheoThang", nhanDoanhThuTheoThang);
        stats.put("DoanhThuTheoThang", doanhThuTheoThang);

        return stats;
    }

    // 2. TỰ ĐỘNG TẠO MÃ SẢN PHẨM TĂNG DẦN (SP001, SP002...)
    public String generateMaSP() {
        return sanPhamRepository.findTopByOrderByMaSPDesc()
                .map(sp -> {
                    int num = Integer.parseInt(sp.getMaSP().substring(2));
                    return String.format("SP%03d", ++num);
                }).orElse("SP001");
    }

    // 3. XỬ LÝ NHẬP HÀNG THỦ CÔNG & UPLOAD ẢNH
    @Transactional
    public void nhapHangThuCong(SanPham sp, MoTa mt, MultipartFile fileAnh) throws Exception {
        String maSP = generateMaSP();
        LocalDateTime now = LocalDateTime.now();

        sp.setMaSP(maSP);
        sp.setNgayCN(now);
        sanPhamRepository.save(sp);

        mt.setMaMT("MT" + maSP.substring(2));
        mt.setMaSP(maSP);
        // Lưu mô tả (Giả định có MoTaRepository)
        
        if (fileAnh != null && !fileAnh.isEmpty()) {
            String fileName = maSP + "_" + fileAnh.getOriginalFilename();
            Path path = Paths.get("src/main/resources/static/Content/HinhAnhPhongVu/" + fileName);
            Files.createDirectories(path.getParent());
            Files.write(path, fileAnh.getBytes());
            // Lưu fileName vào bảng danh sách ảnh của bạn...
        }
    }

    // 4. XỬ LÝ ĐỌC FILE CSV NÂNG CAO
    @Transactional
    public Map<String, Integer> nhapHangTuCSV(MultipartFile fileCSV) throws Exception {
        Map<String, Integer> result = new HashMap<>();
        int countNew = 0, countUpdate = 0;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(fileCSV.getInputStream(), "UTF-8"))) {
            String line = br.readLine(); // Bỏ qua Header line
            int currentMaxNum = Integer.parseInt(generateMaSP().substring(2)) - 1;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] values = line.split(",");
                if (values.length >= 3) {
                    String tenSP = values[0].trim();
                    BigDecimal giaMoi = new BigDecimal(values[1].trim());
                    int soLuongNhap = Integer.parseInt(values[2].trim());

                    Optional<SanPham> spOpt = sanPhamRepository.findByTenSPIgnoreCase(tenSP);
                    if (spOpt.isPresent()) {
                        SanPham sp = spOpt.get();
                        sp.setSoLuongTon((sp.getSoLuongTon() != null ? sp.getSoLuongTon() : 0) + soLuongNhap);
                        sp.setDonGiaSP(giaMoi);
                        sp.setNgayCN(LocalDateTime.now());
                        sanPhamRepository.save(sp);
                        countUpdate++;
                    } else {
                        currentMaxNum++;
                        SanPham sp = new SanPham();
                        sp.setMaSP(String.format("SP%03d", currentMaxNum));
                        sp.setTenSP(tenSP);
                        sp.setDonGiaSP(giaMoi);
                        sp.setSoLuongTon(soLuongNhap);
                        sp.setDonVT(values.length > 3 ? values[3].trim() : "Cái");
                        sp.setNgayCN(LocalDateTime.now());
                        sanPhamRepository.save(sp);
                        countNew++;
                    }
                }
            }
        }
        result.put("new", countNew);
        result.put("update", countUpdate);
        return result;
    }
}
