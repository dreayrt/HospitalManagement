(function () {
    const scheduleListContainer = document.getElementById('scheduleListContainer');
    const searchInput = document.getElementById('scheduleDoctorSearch');
    
    if (!scheduleListContainer) return;

    let doctorsData = [];
    let currentlyOpenDoctorId = null;

    // Fetch API wrapper
    async function fetchJson(url, options = {}) {
        const response = await fetch(url, {
            headers: { 'Content-Type': 'application/json' },
            credentials: 'same-origin',
            ...options
        });
        if (response.status === 204) return null;
        const data = await response.json().catch(() => ({}));
        if (!response.ok) throw new Error(data.message || 'Yêu cầu thất bại');
        return data;
    }

    // Load initial doctor list
    window.loadScheduleDoctors = async function() {
        if(scheduleListContainer) {
            scheduleListContainer.innerHTML = `
                <div style="text-align:center;padding:2rem;color:var(--text-muted); background: var(--bg-card); border-radius: 12px;">
                    <div class="spinner-border text-primary" role="status"></div>
                    <div style="margin-top: 10px;">Đang tải danh sách...</div>
                </div>
            `;
        }
        
        try {
            doctorsData = await fetchJson('/api/doctors/findAll');
            renderScheduleDoctors(doctorsData);
        } catch (error) {
            scheduleListContainer.innerHTML = `<div style="padding: 2rem; color: var(--danger); text-align: center;">Lỗi tải dữ liệu: ${error.message}</div>`;
        }
    }

    // Helper: Build initials
    function buildInitials(name) {
        if (!name) return 'BS';
        return name.split(' ').filter(Boolean).slice(-2).map(p => p.charAt(0).toUpperCase()).join('');
    }

    // Helper: Status badge for Schedule
    function scheduleStatusBadge(status) {
        if (status === 'AVAILABLE') return '<span class="status-badge status-success">Đi làm</span>';
        if (status === 'UNAVAILABLE') return '<span class="status-badge status-danger">Nghỉ / Bận</span>';
        return `<span class="status-badge status-warning">${status}</span>`;
    }

    // Render list of doctors
    function renderScheduleDoctors(doctors) {
        if (!doctors || doctors.length === 0) {
            scheduleListContainer.innerHTML = '<div style="padding: 2rem; text-align: center; color: var(--text-muted); background: var(--bg-card); border-radius: 12px;">Không tìm thấy bác sĩ nào.</div>';
            return;
        }

        scheduleListContainer.innerHTML = doctors.map(doctor => `
            <div class="schedule-accordion-item" id="schedule-accordion-${doctor.id}">
                <div class="schedule-accordion-header" onclick="toggleScheduleAccordion(${doctor.id})">
                    <div class="doctor-info">
                        <div class="doctor-avatar bg-${(doctor.id % 5) + 1}">${buildInitials(doctor.fullName)}</div>
                        <div class="doctor-details">
                            <h5>${doctor.fullName || 'Chưa cập nhật tên'}</h5>
                            <p><i class="bi bi-hospital me-1"></i> ${doctor.departmentName || 'Chưa xếp khoa'} | <i class="bi bi-award me-1"></i> ${doctor.specialtyName || doctor.specialization || 'Chưa rõ'}</p>
                        </div>
                    </div>
                    <div>
                        <span class="status-badge ${doctor.status === 'ACTIVE' ? 'status-success' : 'status-danger'}">${doctor.status}</span>
                    </div>
                    <i class="bi bi-chevron-down chevron-icon"></i>
                </div>
                <div class="schedule-accordion-body" id="schedule-body-${doctor.id}">
                    <div class="schedule-body-content">
                        <div class="schedule-actions">
                            <button class="btn-gradient-primary btn-sm" onclick="showAddScheduleForm(${doctor.id})">
                                <i class="bi bi-plus-lg"></i> Thêm lịch mới
                            </button>
                        </div>
                        
                        <!-- Form thêm / sửa lịch (ẩn mặc định) -->
                        <div id="schedule-form-container-${doctor.id}" style="display: none; background: #fff; padding: 1rem; border-radius: 8px; border: 1px solid var(--border-color); margin-bottom: 1rem; box-shadow: var(--shadow-sm);">
                            <h6 style="margin-bottom: 1rem; font-weight: 700; color: var(--text-dark);" id="schedule-form-title-${doctor.id}">Thêm lịch làm việc</h6>
                            <form id="schedule-form-${doctor.id}" onsubmit="submitScheduleForm(event, ${doctor.id})">
                                <input type="hidden" id="schedule-id-${doctor.id}">
                                <div class="row g-3">
                                    <div class="col-md-3">
                                        <label class="form-label">Từ ngày</label>
                                        <input type="date" class="form-control" id="schedule-start-date-${doctor.id}" onchange="window.checkScheduleDateRange(${doctor.id})" required>
                                    </div>
                                    <div class="col-md-3">
                                        <label class="form-label">Đến ngày</label>
                                        <input type="date" class="form-control" id="schedule-end-date-${doctor.id}" onchange="window.checkScheduleDateRange(${doctor.id})" required>
                                    </div>
                                    <div class="col-md-3">
                                        <label class="form-label">Giờ bắt đầu</label>
                                        <input type="time" class="form-control" id="schedule-start-${doctor.id}" required>
                                    </div>
                                    <div class="col-md-3">
                                        <label class="form-label">Giờ kết thúc</label>
                                        <input type="time" class="form-control" id="schedule-end-${doctor.id}" required>
                                    </div>
                                    
                                    <div class="col-12" id="schedule-days-container-${doctor.id}" style="display: none; margin-top: 10px;">
                                        <label class="form-label">Lặp lại vào các ngày trong tuần:</label>
                                        <div class="d-flex gap-2 flex-wrap">
                                            <div class="form-check"><input class="form-check-input day-chk-${doctor.id}" type="checkbox" value="1" checked id="chk1-${doctor.id}"><label class="form-check-label" for="chk1-${doctor.id}">T2</label></div>
                                            <div class="form-check"><input class="form-check-input day-chk-${doctor.id}" type="checkbox" value="2" checked id="chk2-${doctor.id}"><label class="form-check-label" for="chk2-${doctor.id}">T3</label></div>
                                            <div class="form-check"><input class="form-check-input day-chk-${doctor.id}" type="checkbox" value="3" checked id="chk3-${doctor.id}"><label class="form-check-label" for="chk3-${doctor.id}">T4</label></div>
                                            <div class="form-check"><input class="form-check-input day-chk-${doctor.id}" type="checkbox" value="4" checked id="chk4-${doctor.id}"><label class="form-check-label" for="chk4-${doctor.id}">T5</label></div>
                                            <div class="form-check"><input class="form-check-input day-chk-${doctor.id}" type="checkbox" value="5" checked id="chk5-${doctor.id}"><label class="form-check-label" for="chk5-${doctor.id}">T6</label></div>
                                            <div class="form-check"><input class="form-check-input day-chk-${doctor.id}" type="checkbox" value="6" id="chk6-${doctor.id}"><label class="form-check-label" for="chk6-${doctor.id}">T7</label></div>
                                            <div class="form-check"><input class="form-check-input day-chk-${doctor.id}" type="checkbox" value="0" id="chk0-${doctor.id}"><label class="form-check-label" for="chk0-${doctor.id}">CN</label></div>
                                        </div>
                                    </div>

                                    <div class="col-md-3 mt-3">
                                        <label class="form-label">Trạng thái</label>
                                        <select class="form-select" id="schedule-status-${doctor.id}" required>
                                            <option value="AVAILABLE">Đi làm</option>
                                            <option value="UNAVAILABLE">Nghỉ / Bận</option>
                                        </select>
                                    </div>
                                </div>
                                <div class="mt-3 text-end">
                                    <button type="button" class="btn btn-secondary btn-sm" onclick="hideScheduleForm(${doctor.id})">Hủy</button>
                                    <button type="submit" class="btn btn-primary btn-sm ms-2" id="btn-save-schedule-${doctor.id}">Lưu lịch</button>
                                </div>
                            </form>
                        </div>

                        <!-- Bảng lịch làm việc -->
                        <div id="schedule-table-container-${doctor.id}">
                            <div class="text-center py-3 text-muted"><div class="spinner-border spinner-border-sm text-primary"></div> Đang tải lịch...</div>
                        </div>
                    </div>
                </div>
            </div>
        `).join('');
    }

    // Filter local doctors
    window.filterScheduleDoctors = function() {
        const query = (searchInput.value || '').toLowerCase();
        if(!query) {
            renderScheduleDoctors(doctorsData);
            return;
        }
        const filtered = doctorsData.filter(d => 
            (d.fullName && d.fullName.toLowerCase().includes(query)) ||
            (d.departmentName && d.departmentName.toLowerCase().includes(query)) ||
            (d.specialtyName && d.specialtyName.toLowerCase().includes(query))
        );
        renderScheduleDoctors(filtered);
    }

    // Toggle Accordion
    window.toggleScheduleAccordion = async function(doctorId) {
        const item = document.getElementById(`schedule-accordion-${doctorId}`);
        
        // If clicking the currently open one, close it
        if (currentlyOpenDoctorId === doctorId && item.classList.contains('active')) {
            item.classList.remove('active');
            currentlyOpenDoctorId = null;
            return;
        }

        // Close previous active item
        if (currentlyOpenDoctorId) {
            const prevItem = document.getElementById(`schedule-accordion-${currentlyOpenDoctorId}`);
            if (prevItem) prevItem.classList.remove('active');
        }

        // Open new item
        item.classList.add('active');
        currentlyOpenDoctorId = doctorId;

        // Load schedules for this doctor
        await loadSchedulesForDoctor(doctorId);
    }

    // Load schedules from API
    async function loadSchedulesForDoctor(doctorId) {
        const container = document.getElementById(`schedule-table-container-${doctorId}`);
        try {
            const schedules = await fetchJson(`/api/doctors/${doctorId}/schedules`);
            if (!schedules || schedules.length === 0) {
                container.innerHTML = `<div class="text-center py-3 text-muted" style="background: rgba(0,0,0,0.02); border-radius: 8px;">Bác sĩ này chưa có lịch làm việc nào.</div>`;
                return;
            }

            // Sort schedules by date descending
            schedules.sort((a, b) => new Date(b.workDate) - new Date(a.workDate));

            let tableHtml = `
                <table class="schedule-table">
                    <thead>
                        <tr>
                            <th>Ngày làm việc</th>
                            <th>Thời gian</th>
                            <th>Trạng thái</th>
                            <th style="text-align: right;">Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
            `;

            tableHtml += schedules.map(s => `
                <tr>
                    <td style="font-weight: 600;">${s.workDate}</td>
                    <td>${s.startTime.substring(0,5)} - ${s.endTime.substring(0,5)}</td>
                    <td>${scheduleStatusBadge(s.status)}</td>
                    <td style="text-align: right;">
                        <button class="btn btn-sm btn-light text-primary me-1" onclick="editSchedule(${doctorId}, ${s.id})" title="Sửa">
                            <i class="bi bi-pencil"></i>
                        </button>
                        <button class="btn btn-sm btn-light text-danger" onclick="deleteSchedule(${doctorId}, ${s.id})" title="Xóa">
                            <i class="bi bi-trash"></i>
                        </button>
                    </td>
                </tr>
            `).join('');

            tableHtml += `</tbody></table>`;
            container.innerHTML = tableHtml;

        } catch (error) {
            container.innerHTML = `<div class="text-center py-3 text-danger">Lỗi tải lịch: ${error.message}</div>`;
        }
    }

    window.checkScheduleDateRange = function(doctorId) {
        const start = document.getElementById(`schedule-start-date-${doctorId}`).value;
        const end = document.getElementById(`schedule-end-date-${doctorId}`).value;
        const daysContainer = document.getElementById(`schedule-days-container-${doctorId}`);
        if (start && end && start !== end) {
            daysContainer.style.display = 'block';
        } else {
            daysContainer.style.display = 'none';
        }
    }

    // Form Interactions
    window.showAddScheduleForm = function(doctorId) {
        document.getElementById(`schedule-form-container-${doctorId}`).style.display = 'block';
        document.getElementById(`schedule-form-title-${doctorId}`).innerText = 'Thêm lịch làm việc mới';
        document.getElementById(`schedule-form-${doctorId}`).reset();
        document.getElementById(`schedule-id-${doctorId}`).value = '';
        document.getElementById(`schedule-end-date-${doctorId}`).disabled = false;
        window.checkScheduleDateRange(doctorId);
    }

    window.hideScheduleForm = function(doctorId) {
        document.getElementById(`schedule-form-container-${doctorId}`).style.display = 'none';
    }

    window.editSchedule = async function(doctorId, scheduleId) {
        try {
            const schedules = await fetchJson(`/api/doctors/${doctorId}/schedules`);
            const s = schedules.find(item => item.id === scheduleId);
            if(s) {
                document.getElementById(`schedule-form-container-${doctorId}`).style.display = 'block';
                document.getElementById(`schedule-form-title-${doctorId}`).innerText = 'Cập nhật lịch làm việc';
                
                document.getElementById(`schedule-id-${doctorId}`).value = s.id;
                document.getElementById(`schedule-start-date-${doctorId}`).value = s.workDate;
                document.getElementById(`schedule-end-date-${doctorId}`).value = s.workDate; // Editing a single day
                document.getElementById(`schedule-end-date-${doctorId}`).disabled = true; // Disable range editing for updates
                document.getElementById(`schedule-start-${doctorId}`).value = s.startTime;
                document.getElementById(`schedule-end-${doctorId}`).value = s.endTime;
                document.getElementById(`schedule-status-${doctorId}`).value = s.status;
                
                window.checkScheduleDateRange(doctorId);
                
                // Scroll to form
                document.getElementById(`schedule-form-container-${doctorId}`).scrollIntoView({ behavior: 'smooth', block: 'center' });
            }
        } catch(e) {
            showCustomAlert('Lỗi tải dữ liệu', "Lỗi tải thông tin lịch: " + e.message, 'error');
        }
    }

    window.submitScheduleForm = async function(event, doctorId) {
        event.preventDefault();
        const scheduleId = document.getElementById(`schedule-id-${doctorId}`).value;
        const startDateStr = document.getElementById(`schedule-start-date-${doctorId}`).value;
        const endDateStr = document.getElementById(`schedule-end-date-${doctorId}`).value || startDateStr;
        const startTime = document.getElementById(`schedule-start-${doctorId}`).value;
        const endTime = document.getElementById(`schedule-end-${doctorId}`).value;
        const status = document.getElementById(`schedule-status-${doctorId}`).value;

        const btnSave = document.getElementById(`btn-save-schedule-${doctorId}`);
        btnSave.disabled = true;
        btnSave.innerText = 'Đang lưu...';

        try {
            if (scheduleId) {
                // Update single
                const payload = { workDate: startDateStr, startTime, endTime, status };
                await fetchJson(`/api/doctors/${doctorId}/schedules/${scheduleId}`, {
                    method: 'PUT',
                    body: JSON.stringify(payload)
                });
            } else {
                // Create potentially multiple
                const start = new Date(startDateStr);
                const end = new Date(endDateStr);
                
                if (end < start) {
                    throw new Error("Ngày kết thúc không được nhỏ hơn Ngày bắt đầu.");
                }

                const selectedDays = Array.from(document.querySelectorAll(`.day-chk-${doctorId}:checked`)).map(cb => parseInt(cb.value));
                const promises = [];

                // Loop through dates
                let current = new Date(start);
                while (current <= end) {
                    if (selectedDays.includes(current.getDay())) {
                        const isoDate = current.toISOString().split('T')[0];
                        const payload = { workDate: isoDate, startTime, endTime, status };
                        promises.push(fetchJson(`/api/doctors/${doctorId}/schedules`, {
                            method: 'POST',
                            body: JSON.stringify(payload)
                        }));
                    }
                    current.setDate(current.getDate() + 1);
                }

                if (promises.length === 0) {
                    throw new Error("Không có ngày nào hợp lệ được chọn trong khoảng thời gian.");
                }

                await Promise.all(promises);
            }
            
            // Success
            hideScheduleForm(doctorId);
            await loadSchedulesForDoctor(doctorId);
            
            // Show toast if available
            const toast = document.getElementById('doctorToast');
            if(toast) {
                toast.textContent = "Lưu lịch làm việc thành công!";
                toast.className = 'doctor-toast show success';
                setTimeout(() => toast.className = 'doctor-toast', 3000);
            } else {
                showCustomAlert('Thành công', "Lưu lịch thành công!", 'success');
            }

        } catch (error) {
            showCustomAlert('Lỗi lưu lịch', error.message, 'error');
        } finally {
            btnSave.disabled = false;
            btnSave.innerText = 'Lưu lịch';
        }
    }

    window.deleteSchedule = async function(doctorId, scheduleId) {
        const confirmed = await window.showCustomConfirm('Xác nhận xóa', 'Bạn có chắc chắn muốn xóa lịch làm việc này?', 'warning');
        if(!confirmed) return;
        try {
            await fetchJson(`/api/doctors/${doctorId}/schedules/${scheduleId}`, {
                method: 'DELETE'
            });
            await loadSchedulesForDoctor(doctorId);
            
            // Show toast if available
            const toast = document.getElementById('doctorToast');
            if(toast) {
                toast.textContent = "Xóa lịch thành công!";
                toast.className = 'doctor-toast show success';
                setTimeout(() => toast.className = 'doctor-toast', 3000);
            }
        } catch(error) {
            showCustomAlert('Lỗi xóa lịch', error.message, 'error');
        }
    }

    // Auto load initially or listen for section change
    // Usually AdminDashboard has a mechanism to switch sections.
    // Let's attach to the sidebar nav link for 'schedules'
    const scheduleNavLinks = document.querySelectorAll('.sidebar-nav-link[data-section="schedules"]');
    scheduleNavLinks.forEach(link => {
        link.addEventListener('click', () => {
            loadScheduleDoctors();
        });
    });

    // We can also trigger load initially if section is active (though not typical)
    // loadScheduleDoctors();
})();
