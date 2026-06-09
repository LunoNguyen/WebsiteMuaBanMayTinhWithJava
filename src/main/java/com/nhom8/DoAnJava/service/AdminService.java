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
import com.nhom8.DoAnJava.model.CapNhatGia;
import com.nhom8.DoAnJava.model.DanhSachAnh;
import com.nhom8.DoAnJava.model.NhaCungCap; // 🔥 Đã import NhaCungCap
import com.nhom8.DoAnJava.repository.ChiTietHoaDonRepository;
import com.nhom8.DoAnJava.repository.HoaDonRepository;
import com.nhom8.DoAnJava.repository.LoaiSanPhamRepository;
import com.nhom8.DoAnJava.repository.NhaSanXuatRepository;
import com.nhom8.DoAnJava.repository.NhaCungCapRepository;
import com.nhom8.DoAnJava.repository.NhanVienRepository;
import com.nhom8.DoAnJava.repository.SanPhamRepository;
import com.nhom8.DoAnJava.repository.TaiKhoanRepository;
import com.nhom8.DoAnJava.repository.CapNhatGiaRepository;
import com.nhom8.DoAnJava.repository.MoTaRepository;
import com.nhom8.DoAnJava.repository.DanhSachAnhRepository;

@Service
public class AdminService {

    @Autowired private SanPhamRepository sanPhamRepository;
    @Autowired private NhaSanXuatRepository nhaSanXuatRepository;
    @Autowired private LoaiSanPhamRepository loaiSanPhamRepository;
    @Autowired private NhaCungCapRepository nhaCungCapRepository;
    @Autowired private HoaDonRepository hoaDonRepository;
    @Autowired private ChiTietHoaDonRepository chiTietHoaDonRepository;
    @Autowired private NhanVienRepository nhanVienRepository;
    @Autowired private TaiKhoanRepository taiKhoanRepository;

    @Autowired private CapNhatGiaRepository capNhatGiaRepository;
    @Autowired private MoTaRepository moTaRepository;
    @Autowired private DanhSachAnhRepository danhSachAnhRepository;

    // 1. TÍNH TOÁN DOANH THU & THỐNG KÊ DASHBOARD
    public Map<String, Object> getDashboardStats(int thang, int nam) {
        Map<String, Object> stats = new HashMap<>();

        List<HoaDon> hoaDons = hoaDonRepository.findByThangAndNam(thang, nam);

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

    // TỰ ĐỘNG TẠO MÃ MÔ TẢ TĂNG DẦN (MT001, MT002...)
    public String generateMaMoTa() {
        return moTaRepository.findAll().stream()
                .map(MoTa::getMaMT)
                .filter(Objects::nonNull)
                .filter(id -> id.startsWith("MT"))
                .map(id -> {
                    try {
                        return Integer.parseInt(id.substring(2));
                    } catch (Exception e) {
                        return 0;
                    }
                })
                .max(Integer::compare)
                .map(num -> String.format("MT%03d", num + 1))
                .orElse("MT001");
    }

    // TỰ ĐỘNG TẠO MÃ DANH SÁCH ẢNH TĂNG DẦN (DSA001, DSA002...)
    private synchronized String generateMaDSA() {
        DanhSachAnh last = danhSachAnhRepository.findTopByOrderByMaDsaDesc();
        if (last == null || last.getMaDsa() == null) {
            return "DSA001";
        }
        try {
            int next = Integer.parseInt(last.getMaDsa().substring(3)) + 1;
            return String.format("DSA%03d", next);
        } catch (Exception e) {
            return "DSA" + (System.currentTimeMillis() % 1000000);
        }
    }

    // TỰ ĐỘNG TẠO MÃ NHÀ CUNG CẤP TĂNG DẦN (NCC001, NCC002...)
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

    // 3. XỬ LÝ NHẬP HÀNG THỦ CÔNG & UPLOAD ẢNH
    @Transactional
    public void nhapHangThuCong(SanPham sp, MoTa mt, MultipartFile fileAnh) {
        try {
            LocalDateTime ngayCN = LocalDateTime.now().withNano(0);

            // Kiểm tra sản phẩm đã tồn tại chưa (theo tên, không phân biệt hoa thường)
            Optional<SanPham> spCu = sanPhamRepository.findByTenSPIgnoreCase(sp.getTenSP().trim());

            if (spCu.isPresent()) {
                // Sản phẩm đã có → chỉ tăng số lượng tồn + cập nhật giá nếu có
                SanPham spHienTai = spCu.get();
                int soLuongNhapThem = (sp.getSoLuongTon() != null ? sp.getSoLuongTon() : 0);
                spHienTai.setSoLuongTon((spHienTai.getSoLuongTon() != null ? spHienTai.getSoLuongTon() : 0) + soLuongNhapThem);
                spHienTai.setDonGiaSP(sp.getDonGiaSP());
                spHienTai.setNgayCN(ngayCN);

                CapNhatGia capNhatGia = new CapNhatGia();
                capNhatGia.setNgayCN(ngayCN);
                capNhatGia.setDonGiaCN(sp.getDonGiaSP());
                capNhatGiaRepository.saveAndFlush(capNhatGia);

                sanPhamRepository.save(spHienTai);
            } else {
                // Sản phẩm mới hoàn toàn → tạo mới
                CapNhatGia capNhatGia = new CapNhatGia();
                capNhatGia.setNgayCN(ngayCN);
                capNhatGia.setDonGiaCN(sp.getDonGiaSP());
                capNhatGiaRepository.saveAndFlush(capNhatGia);

                String maSP = generateMaSP();
                sp.setMaSP(maSP);
                sp.setNgayCN(ngayCN);

                if (sp.getSoLuongTon() == null) {
                    sp.setSoLuongTon(0);
                }
                if (sp.getDonVT() == null || sp.getDonVT().trim().isEmpty()) {
                    sp.setDonVT("Cái");
                }

                sanPhamRepository.save(sp);

                if (mt != null && mt.getCpu() != null && !mt.getCpu().trim().isEmpty()) {
                    String maMT = generateMaMoTa();
                    mt.setMaMT(maMT);
                    mt.setMaSP(maSP);
                    moTaRepository.save(mt);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Lỗi xử lý nghiệp vụ nhập hàng: " + e.getMessage());
        }
    }

    // 4. XỬ LÝ ĐỌC FILE CSV NÂNG CAO (HỖ TRỢ ĐỂN 12 CỘT & TỰ ĐỘNG TẠO NCC MỚI)
    @Transactional
    public Map<String, Integer> nhapHangTuCSV(MultipartFile fileCSV) throws Exception {
        Map<String, Integer> result = new HashMap<>();
        int countNew = 0, countUpdate = 0;

        // Lấy phân loại mặc định đề phòng file trống thông tin
        String defaultLoai = loaiSanPhamRepository.findAll().stream().findFirst().map(l -> l.getMaLoai()).orElse(null);
        String defaultNSX = nhaSanXuatRepository.findAll().stream().findFirst().map(n -> n.getMaNSX()).orElse(null);
        String defaultNCC = nhaCungCapRepository.findAll().stream().findFirst().map(ncc -> ncc.getMaNCC()).orElse(null);

        try (BufferedReader br = new BufferedReader(new InputStreamReader(fileCSV.getInputStream(), "UTF-8"))) {
            String headerLine = br.readLine(); // Bỏ qua dòng Header
            int currentMaxNum = Integer.parseInt(generateMaSP().substring(2)) - 1;

            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] values = line.split(",");
                if (values.length >= 3) {
                    String tenSP = values[0].trim();
                    BigDecimal giaMoi = new BigDecimal(values[1].trim());
                    int soLuongNhap = Integer.parseInt(values[2].trim());

                    // Các cột cấu hình nâng cao trong file CSV
                    String donVi = (values.length > 3 && !values[3].trim().isEmpty()) ? values[3].trim() : "Cái";
                    String ram = (values.length > 4) ? values[4].trim() : "";
                    String cpu = (values.length > 5) ? values[5].trim() : "";
                    String rom = (values.length > 6) ? values[6].trim() : "";
                    String manHinh = (values.length > 7) ? values[7].trim() : "";
                    String vga = (values.length > 8) ? values[8].trim() : "";
                    String khac = (values.length > 9) ? values[9].trim() : "";
                    String tenAnh = (values.length > 10) ? values[10].trim() : "";

                    // Cột thứ 12: Tên nhà cung cấp (TenNCC)
                    String tenNCC = (values.length > 11) ? values[11].trim() : "";

                    LocalDateTime ngayCN = LocalDateTime.now().withNano(0);

                    // Tìm mã Nhà Cung Cấp dựa trên Tên nhà cung cấp từ CSV
                    String selectedMaNCC = defaultNCC;
                    if (!tenNCC.isEmpty()) {
                        // Tìm xem tên nhà cung cấp này đã tồn tại trong database chưa
                        Optional<NhaCungCap> nccOpt = nhaCungCapRepository.findAll().stream()
                                .filter(n -> n.getTenNCC().equalsIgnoreCase(tenNCC))
                                .findFirst();

                        if (nccOpt.isPresent()) {
                            selectedMaNCC = nccOpt.get().getMaNCC();
                        } else {
                            // Nếu chưa có, tự động tạo mới nhà cung cấp luôn
                            NhaCungCap nccMoi = new NhaCungCap();
                            selectedMaNCC = generateMaNCC();
                            nccMoi.setMaNCC(selectedMaNCC);
                            nccMoi.setTenNCC(tenNCC);
                            nccMoi.setSdtNCC(""); // Mặc định để trống SĐT và Địa chỉ khi import nhanh qua CSV
                            nccMoi.setDiaChiNCC("");
                            nhaCungCapRepository.save(nccMoi);
                        }
                    }

                    Optional<SanPham> spOpt = sanPhamRepository.findByTenSPIgnoreCase(tenSP);
                    if (spOpt.isPresent()) {
                        // Trường hợp: SẢN PHẨM ĐÃ CÓ - Cập nhật giá, số lượng và cập nhật nhà cung cấp nếu có ghi nhận mới
                        SanPham sp = spOpt.get();
                        sp.setSoLuongTon((sp.getSoLuongTon() != null ? sp.getSoLuongTon() : 0) + soLuongNhap);
                        sp.setDonGiaSP(giaMoi);
                        sp.setNgayCN(ngayCN);

                        if (!tenNCC.isEmpty()) {
                            sp.setMaNCC(selectedMaNCC);
                        }

                        CapNhatGia capNhatGia = new CapNhatGia();
                        capNhatGia.setNgayCN(ngayCN);
                        capNhatGia.setDonGiaCN(giaMoi);
                        capNhatGiaRepository.saveAndFlush(capNhatGia);

                        sanPhamRepository.save(sp);
                        countUpdate++;
                    } else {
                        // Trường hợp: SẢN PHẨM MỚI HOÀN TOÀN
                        currentMaxNum++;
                        String maSP = String.format("SP%03d", currentMaxNum);

                        SanPham sp = new SanPham();
                        sp.setMaSP(maSP);
                        sp.setTenSP(tenSP);
                        sp.setDonGiaSP(giaMoi);
                        sp.setSoLuongTon(soLuongNhap);
                        sp.setDonVT(donVi);
                        sp.setNgayCN(ngayCN);

                        // Gán các khóa ngoại
                        sp.setMaLoai(defaultLoai);
                        sp.setMaNSX(defaultNSX);
                        sp.setMaNCC(selectedMaNCC); // Gán mã nhà cung cấp thông minh vừa tìm/tạo được

                        // Lưu cập nhật giá
                        CapNhatGia capNhatGia = new CapNhatGia();
                        capNhatGia.setNgayCN(ngayCN);
                        capNhatGia.setDonGiaCN(giaMoi);
                        capNhatGiaRepository.saveAndFlush(capNhatGia);

                        sanPhamRepository.save(sp);

                        // Lưu cấu hình chi tiết (Bảng MOTA)
                        if (!cpu.isEmpty() || !ram.isEmpty() || !rom.isEmpty()) {
                            MoTa mt = new MoTa();
                            mt.setMaMT(generateMaMoTa());
                            mt.setMaSP(maSP);
                            mt.setCpu(cpu);
                            mt.setRam(ram);
                            mt.setRom(rom);
                            mt.setManHinh(manHinh);
                            mt.setVga(vga);
                            mt.setKhac(khac);
                            moTaRepository.save(mt);
                        }

                        // Lưu Tên Ảnh (Bảng DANHSACHANH)
                        if (!tenAnh.isEmpty()) {
                            DanhSachAnh dsa = new DanhSachAnh();
                            dsa.setMaDsa(generateMaDSA());
                            dsa.setSanPham(sp);
                            dsa.setTenAnh(tenAnh);
                            danhSachAnhRepository.save(dsa);
                        }

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