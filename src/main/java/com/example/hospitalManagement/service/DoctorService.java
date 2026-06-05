package com.example.hospitalManagement.service;

import com.example.hospitalManagement.dto.DoctorDTO;
import com.example.hospitalManagement.dto.DoctorFilterRequest;
import com.example.hospitalManagement.dto.DoctorScheduleDTO;
import com.example.hospitalManagement.entity.Departments;
import com.example.hospitalManagement.entity.Doctor;
import com.example.hospitalManagement.entity.DoctorSchedules;
import com.example.hospitalManagement.entity.Enum.DepartmentStatus;
import com.example.hospitalManagement.entity.Enum.DoctorSchedulesStatus;
import com.example.hospitalManagement.entity.Enum.UserStatus;
import com.example.hospitalManagement.entity.Role;
import com.example.hospitalManagement.entity.User;
import com.example.hospitalManagement.mapper.DoctorMapper;
import com.example.hospitalManagement.repository.DeparmentRepository;
import com.example.hospitalManagement.repository.DoctorRepository;
import com.example.hospitalManagement.repository.DoctorScheduleRepository;
import com.example.hospitalManagement.repository.RoleRepository;
import com.example.hospitalManagement.repository.userRepository;
import com.example.hospitalManagement.util.HashUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class DoctorService {
    private static final String DOCTOR_CACHE_VERSION_KEY = "doctors:cache:version:v2";
    private static final String DOCTOR_DEPARTMENTS_CACHE_KEY = "doctors:departments:active";
    private static final long DOCTOR_CACHE_TTL_SECONDS = 120;
    private static final long DOCTOR_DEPARTMENT_CACHE_TTL_SECONDS = 300;

    private final DoctorRepository doctorRepository;
    private final DoctorScheduleRepository doctorScheduleRepository;
    private final DeparmentRepository deparmentRepository;
    private final userRepository userRepository;
    private final RoleRepository roleRepository;
    private final DoctorMapper doctorMapper;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;

    @PersistenceContext
    private EntityManager entityManager;

    public DoctorService(
            DoctorRepository doctorRepository,
            DoctorScheduleRepository doctorScheduleRepository,
            DeparmentRepository deparmentRepository,
            userRepository userRepository,
            RoleRepository roleRepository,
            DoctorMapper doctorMapper,
            RedisService redisService,
            ObjectMapper objectMapper
    ) {
        this.doctorRepository = doctorRepository;
        this.doctorScheduleRepository = doctorScheduleRepository;
        this.deparmentRepository = deparmentRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.doctorMapper = doctorMapper;
        this.redisService = redisService;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<DoctorDTO> findAll(DoctorFilterRequest filterRequest) {
        String cacheKey = buildDoctorFindAllCacheKey(filterRequest);
        List<DoctorDTO> cachedDoctors = readCache(cacheKey, new TypeReference<List<DoctorDTO>>() {});
        if (cachedDoctors != null) {
            return cachedDoctors;
        }

        Specification<Doctor> specification = buildSpecification(filterRequest);
        List<Doctor> doctors = doctorRepository.findAll(specification, Sort.by(Sort.Order.asc("id")));

        List<DoctorDTO> items = doctors.stream()
                .map(this::toDoctorDTO)
                .sorted(Comparator.comparing(DoctorDTO::getFullName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .collect(Collectors.toList());

        if (Boolean.TRUE.equals(filterRequest.getAvailableToday())) {
            items = items.stream()
                    .filter(item -> item.getTodayScheduleCount() > 0)
                    .collect(Collectors.toList());
        }
        writeCache(cacheKey, items, DOCTOR_CACHE_TTL_SECONDS);
        return items;
    }

    @Transactional(readOnly = true)
    public DoctorDTO findById(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Khong tim thay bac si"));
        DoctorDTO dto = toDoctorDTO(doctor);
        dto.setSchedules(findSchedules(id));
        return dto;
    }

    @Transactional
    public DoctorDTO create(DoctorDTO request) {
        validateDoctorRequest(request, null);

        
        User user = (request.getUserId() != null && request.getUserId() > 0)
                ? findUserById(request.getUserId())
                : createDoctorUser(request);

        if (user.getDoctor() != null) {
            throw new IllegalStateException("Tai khoan nay da co ho so bac si");
        }
        validateUserRole(user);

        Departments department = findDepartmentById(request.getDepartmentId());

        Doctor doctor = new Doctor();
        applyDoctorChanges(doctor, request, user, department);
        DoctorDTO result = toDoctorDTO(doctorRepository.save(doctor));
        invalidateDoctorCache();
        return result;
    }

    @Transactional
    public DoctorDTO update(Long id, DoctorDTO request) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Khong tim thay bac si"));
        validateDoctorRequest(request, id);

        User user = findUserById(request.getUserId());
        if (user.getDoctor() != null && user.getDoctor().getId() != doctor.getId()) {
            throw new IllegalStateException("Tai khoan nay da gan voi bac si khac");
        }
        validateUserRole(user);

        Departments department = findDepartmentById(request.getDepartmentId());

        applyDoctorChanges(doctor, request, user, department);
        DoctorDTO result = toDoctorDTO(doctorRepository.save(doctor));
        invalidateDoctorCache();
        return result;
    }

    @Transactional
    public void delete(Long id) {
        
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Khong tim thay bac si"));

        
        if (doctor.getAppointments() != null && !doctor.getAppointments().isEmpty()) {
            throw new IllegalStateException("Khong the xoa bac si da co lich hen");
        }
        if (doctor.getMedicalRecords() != null && !doctor.getMedicalRecords().isEmpty()) {
            throw new IllegalStateException("Khong the xoa bac si da co ho so benh an");
        }
        if (doctor.getDoctorSchedules() != null && !doctor.getDoctorSchedules().isEmpty()) {
            throw new IllegalStateException("Khong the xoa bac si khi van con lich lam viec");
        }

        
        User user = doctor.getUser();

        
        if (user != null) {
            user.setDoctor(null);
        }
        doctor.setUser(null);

        
        doctorRepository.delete(doctor);

        
        if (user != null) {
            userRepository.delete(user);
        }

        
        invalidateDoctorCache();
    }

    @Transactional(readOnly = true)
    public List<DoctorScheduleDTO> findSchedules(Long doctorId) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new NoSuchElementException("Khong tim thay bac si");
        }
        return doctorScheduleRepository.findByDoctorIdOrderByWorkDateAscStartTimeAsc(doctorId).stream()
                .map(doctorMapper::toScheduleDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public DoctorScheduleDTO createSchedule(Long doctorId, DoctorScheduleDTO request) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new NoSuchElementException("Khong tim thay bac si"));
        validateScheduleRequest(doctorId, request, null);

        DoctorSchedules schedule = new DoctorSchedules();
        schedule.setDoctor(doctor);
        schedule.setWorkDate(request.getWorkDate());
        schedule.setStartTime(request.getStartTime());
        schedule.setEndTime(request.getEndTime());
        schedule.setStatus(request.getStatus());
        DoctorScheduleDTO result = doctorMapper.toScheduleDTO(doctorScheduleRepository.save(schedule));
        invalidateDoctorCache();
        return result;
    }

    @Transactional
    public DoctorScheduleDTO updateSchedule(Long doctorId, Long scheduleId, DoctorScheduleDTO request) {
        DoctorSchedules schedule = doctorScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new NoSuchElementException("Khong tim thay lich lam viec"));
        if (schedule.getDoctor() == null || schedule.getDoctor().getId() != doctorId) {
            throw new IllegalStateException("Lich lam viec khong thuoc bac si nay");
        }
        validateScheduleRequest(doctorId, request, scheduleId);

        schedule.setWorkDate(request.getWorkDate());
        schedule.setStartTime(request.getStartTime());
        schedule.setEndTime(request.getEndTime());
        schedule.setStatus(request.getStatus());
        DoctorScheduleDTO result = doctorMapper.toScheduleDTO(doctorScheduleRepository.save(schedule));
        invalidateDoctorCache();
        return result;
    }

    @Transactional
    public void deleteSchedule(Long doctorId, Long scheduleId) {
        DoctorSchedules schedule = doctorScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new NoSuchElementException("Khong tim thay lich lam viec"));
        if (schedule.getDoctor() == null || schedule.getDoctor().getId() != doctorId) {
            throw new IllegalStateException("Lich lam viec khong thuoc bac si nay");
        }
        doctorScheduleRepository.delete(schedule);
        invalidateDoctorCache();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getActivityStatus(Long doctorId) {
        DoctorDTO doctor = findById(doctorId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("doctorId", doctor.getId());
        result.put("doctorName", doctor.getFullName());
        result.put("status", doctor.getStatus());
        result.put("workingStatus", doctor.getWorkingStatus());
        result.put("todayScheduleCount", doctor.getTodayScheduleCount());
        result.put("totalScheduleCount", doctor.getTotalScheduleCount());
        result.put("active", doctor.getStatus() == UserStatus.ACTIVE);
        return result;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> findAllDepartments() {
        List<Map<String, Object>> cachedDepartments = readCache(
                DOCTOR_DEPARTMENTS_CACHE_KEY,
                new TypeReference<List<Map<String, Object>>>() {}
        );
        if (cachedDepartments != null) {
            return cachedDepartments;
        }

        List<Map<String, Object>> departments = deparmentRepository.findByStatus(DepartmentStatus.ACTIVE).stream()
                .map(department -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", department.getId());
                    item.put("name", department.getName());
                    item.put("description", department.getDescription());
                    return item;
                })
                .collect(Collectors.toList());
        writeCache(DOCTOR_DEPARTMENTS_CACHE_KEY, departments, DOCTOR_DEPARTMENT_CACHE_TTL_SECONDS);
        return departments;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> findAvailableUsers(Long doctorId) {
        List<User> users = new ArrayList<>(userRepository.findUsersWithoutDoctorProfile());
        if (doctorId != null) {
            doctorRepository.findById(doctorId)
                    .map(Doctor::getUser)
                    .filter(Objects::nonNull)
                    .ifPresent(user -> {
                        boolean exists = users.stream().anyMatch(item -> item.getId() == user.getId());
                        if (!exists) {
                            users.add(user);
                        }
                    });
        }

        return users.stream()
                .sorted(Comparator.comparing(User::getFullName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .map(user -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", user.getId());
                    item.put("fullName", user.getFullName());
                    item.put("email", user.getEmail());
                    item.put("phone", user.getPhone());
                    item.put("status", user.getStatus());
                    item.put("role", user.getRole() != null ? user.getRole().getName() : null);
                    return item;
                })
                .collect(Collectors.toList());
    }

    private Specification<Doctor> buildSpecification(DoctorFilterRequest filterRequest) {
        return (root, query, builder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(filterRequest.getKeyword())) {
                String keyword = "%" + filterRequest.getKeyword().trim().toLowerCase() + "%";
                var userJoin = root.join("user", JoinType.LEFT);
                predicates.add(builder.or(
                        builder.like(builder.lower(userJoin.get("fullName")), keyword),
                        builder.like(builder.lower(userJoin.get("email")), keyword),
                        builder.like(builder.lower(userJoin.get("phone")), keyword),
                        builder.like(builder.lower(root.get("licenseNumber")), keyword),
                        builder.like(builder.lower(root.get("specialization")), keyword)
                ));
            }

            if (filterRequest.getDepartmentId() != null) {
                predicates.add(builder.equal(root.join("department", JoinType.LEFT).get("id"), filterRequest.getDepartmentId()));
            }

            if (filterRequest.getUserStatus() != null) {
                predicates.add(builder.equal(root.get("userStatus"), filterRequest.getUserStatus()));
            }

            return builder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    private DoctorDTO toDoctorDTO(Doctor doctor) {
        List<DoctorSchedules> schedules = doctorScheduleRepository.findByDoctorIdOrderByWorkDateAscStartTimeAsc(doctor.getId());
        long todayScheduleCount = schedules.stream()
                .filter(schedule -> schedule.getWorkDate() != null && schedule.getWorkDate().isEqual(LocalDate.now()))
                .count();

        DoctorDTO dto = doctorMapper.toDoctorDTO(
                doctor,
                resolveWorkingStatus(doctor, schedules),
                todayScheduleCount,
                schedules.size()
        );
        if (dto.getDepartmentId() != null) {
            dto.setDepartmentName(resolveDepartmentName(dto.getDepartmentId()));
        }

        dto.setSchedules(schedules.stream().map(doctorMapper::toScheduleDTO).collect(Collectors.toList()));
        return dto;
    }

    private String resolveWorkingStatus(Doctor doctor, List<DoctorSchedules> schedules) {
        if (doctor.getUserStatus() == UserStatus.LOCKED) {
            return "Bi khoa";
        }
        if (doctor.getUserStatus() == UserStatus.INACTIVE) {
            return "Tam ngung";
        }

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        boolean onShift = schedules.stream()
                .filter(schedule -> schedule.getStatus() == DoctorSchedulesStatus.AVAILABLE)
                .anyMatch(schedule ->
                        schedule.getWorkDate() != null
                                && schedule.getWorkDate().isEqual(today)
                                && schedule.getStartTime() != null
                                && schedule.getEndTime() != null
                                && !now.isBefore(schedule.getStartTime())
                                && now.isBefore(schedule.getEndTime())
                );
        if (onShift) {
            return "Dang lam viec";
        }

        boolean hasShiftToday = schedules.stream()
                .filter(schedule -> schedule.getStatus() == DoctorSchedulesStatus.AVAILABLE)
                .anyMatch(schedule -> schedule.getWorkDate() != null && schedule.getWorkDate().isEqual(today));
        if (hasShiftToday) {
            return "Co lich hom nay";
        }

        return "Chua xep lich";
    }

    private void updateUserDetails(User user, DoctorDTO request) {
        user.setFullName(request.getFullName().trim());
        user.setEmail(StringUtils.hasText(request.getEmail()) ? request.getEmail().trim() : null);
        user.setPhone(StringUtils.hasText(request.getPhone()) ? request.getPhone().trim() : null);
        user.setStatus(request.getStatus());
        user.setUpdatedAt(LocalDateTime.now());
    }

    private void applyDoctorChanges(Doctor doctor, DoctorDTO request, User user, Departments department) {
        doctor.setUser(user);
        doctor.setDepartment(department);
        doctor.setLicenseNumber(request.getLicenseNumber().trim());
        doctor.setExperienceYears(request.getExperienceYears());
        doctor.setUserStatus(request.getStatus());
        doctor.setSpecialization(request.getSpecialization() != null ? request.getSpecialization().trim() : null);
        updateUserDetails(user, request);
    }

    private void validateDoctorRequest(DoctorDTO request, Long doctorId) {
        if (request == null) {
            throw new IllegalArgumentException("Du lieu bac si khong hop le");
        }
        if (!StringUtils.hasText(request.getFullName())) {
            throw new IllegalArgumentException("Ho ten bac si khong duoc de trong");
        }
        if (!StringUtils.hasText(request.getLicenseNumber())) {
            throw new IllegalArgumentException("So chung chi hanh nghe khong duoc de trong");
        }
        if (request.getExperienceYears() == null || request.getExperienceYears() < 0 || request.getExperienceYears() > 60) {
            throw new IllegalArgumentException("So nam kinh nghiem khong hop le");
        }
        if (request.getDepartmentId() == null) {
            throw new IllegalArgumentException("Khoa phong lam viec khong duoc de trong");
        }
        if (request.getStatus() == null) {
            throw new IllegalArgumentException("Trang thai bac si khong duoc de trong");
        }
        if (doctorId == null && doctorRepository.existsByLicenseNumber(request.getLicenseNumber().trim())) {
            throw new IllegalStateException("So chung chi hanh nghe da ton tai");
        }
        if (doctorId != null && doctorRepository.existsByLicenseNumberAndIdNot(request.getLicenseNumber().trim(), doctorId)) {
            throw new IllegalStateException("So chung chi hanh nghe da ton tai");
        }
    }

    private void validateScheduleRequest(Long doctorId, DoctorScheduleDTO request, Long scheduleId) {
        if (request.getEndTime().isBefore(request.getStartTime()) || request.getEndTime().equals(request.getStartTime())) {
            throw new IllegalArgumentException("Gio ket thuc phai sau gio bat dau");
        }
        if (request.getWorkDate().isBefore(LocalDate.now().minusMonths(1))) {
            throw new IllegalArgumentException("Khong the tao lich qua cu cho bac si");
        }
        if (doctorScheduleRepository.existsOverlappingSchedule(
                doctorId,
                request.getWorkDate(),
                request.getStartTime(),
                request.getEndTime(),
                scheduleId
        )) {
            throw new IllegalStateException("Lich lam viec bi trung voi ca khac cua bac si");
        }
    }

    private void validateUserRole(User user) {
        if (user.getRole() != null
                && StringUtils.hasText(user.getRole().getName())
                && !"DOCTOR".equalsIgnoreCase(user.getRole().getName())) {
            throw new IllegalStateException("Chi tai khoan co role DOCTOR moi duoc gan ho so bac si");
        }
    }

    private Departments findDepartmentById(Long departmentId) {
        if (departmentId == null || !deparmentRepository.existsById(departmentId)) {
            throw new NoSuchElementException("Khong tim thay khoa phong");
        }
        return entityManager.getReference(Departments.class, departmentId);
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Khong tim thay tai khoan nguoi dung"));
    }

    private User createDoctorUser(DoctorDTO request) {
        
        if (!StringUtils.hasText(request.getUserName())) {
            throw new IllegalArgumentException("Ten dang nhap tai khoan bac si khong duoc de trong");
        }
        if (!StringUtils.hasText(request.getPassword())) {
            throw new IllegalArgumentException("Mat khau tai khoan bac si khong duoc de trong");
        }
        if (request.getPassword().trim().length() < 6) {
            throw new IllegalArgumentException("Mat khau tai khoan bac si toi thieu 6 ky tu");
        }
        if (userRepository.existsByUserName(request.getUserName().trim())) {
            throw new IllegalStateException("Ten dang nhap da ton tai");
        }
        if (StringUtils.hasText(request.getEmail()) && userRepository.existsByEmail(request.getEmail().trim())) {
            throw new IllegalStateException("Email nay da duoc su dung boi tai khoan khac");
        }

        Role doctorRole = roleRepository.findByNameIgnoreCase("DOCTOR")
                .orElseThrow(() -> new NoSuchElementException("Khong tim thay role DOCTOR"));

        
        User user = new User();
        user.setUserName(request.getUserName().trim());
        user.setPassword(HashUtil.sha256(request.getPassword().trim())); 
        user.setFullName(request.getFullName().trim());
        user.setEmail(StringUtils.hasText(request.getEmail()) ? request.getEmail().trim() : null);
        user.setPhone(StringUtils.hasText(request.getPhone()) ? request.getPhone().trim() : null);
        user.setStatus(request.getStatus());
        user.setRole(doctorRole);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(null);
        return userRepository.save(user);
    }

    private String resolveDepartmentName(Long departmentId) {
        return findAllDepartments().stream()
                .filter(item -> Objects.equals(String.valueOf(item.get("id")), String.valueOf(departmentId)))
                .map(item -> item.get("name"))
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .findFirst()
                .orElseGet(() -> deparmentRepository.findById(departmentId)
                        .map(Departments::getName)
                        .orElse(null));
    }

    private String buildDoctorFindAllCacheKey(DoctorFilterRequest filterRequest) {
        return "doctors:findAll:v" + getDoctorCacheVersion()
                + ":keyword=" + safeCachePart(filterRequest.getKeyword())
                + ":departmentId=" + safeCachePart(filterRequest.getDepartmentId())
                + ":userStatus=" + safeCachePart(filterRequest.getUserStatus())
                + ":availableToday=" + safeCachePart(filterRequest.getAvailableToday());
    }

    private String getDoctorCacheVersion() {
        Object version = redisService.get(DOCTOR_CACHE_VERSION_KEY);
        if (version == null || String.valueOf(version).isBlank()) {
            redisService.save(DOCTOR_CACHE_VERSION_KEY, "1", 86400);
            return "1";
        }
        return String.valueOf(version);
    }

    private void invalidateDoctorCache() {
        redisService.save(DOCTOR_CACHE_VERSION_KEY, String.valueOf(System.currentTimeMillis()), 86400);
        redisService.remove(DOCTOR_DEPARTMENTS_CACHE_KEY);
    }

    private String safeCachePart(Object value) {
        return value == null ? "all" : String.valueOf(value).trim().replaceAll("\\s+", "_");
    }

    private <T> T readCache(String key, TypeReference<T> typeReference) {
        try {
            if (!redisService.exists(key)) {
                return null;
            }
            Object cached = redisService.get(key);
            if (cached == null) {
                return null;
            }
            return objectMapper.readValue(String.valueOf(cached), typeReference);
        } catch (Exception exception) {
            redisService.remove(key);
            return null;
        }
    }

    private void writeCache(String key, Object value, long ttlSeconds) {
        try {
            redisService.save(key, objectMapper.writeValueAsString(value), ttlSeconds);
        } catch (Exception ignored) {
        }
    }
}