(function () {
    const doctorTableBody = document.getElementById('doctorTableBody');
    if (!doctorTableBody) {
        return;
    }

    const state = {
        doctors: [],
        specialties: [],
        departments: [],
        users: [],
        selectedDoctorId: null
    };

    const scheduleTableBody = document.getElementById('scheduleTableBody');
    const scheduleDoctorInfo = document.getElementById('scheduleDoctorInfo');
    const toastMessage = document.getElementById('doctorToast');

    async function fetchJson(url, options = {}) {
        const response = await fetch(url, {
            headers: { 'Content-Type': 'application/json' },
            credentials: 'same-origin',
            ...options
        });

        if (response.status === 204) {
            return null;
        }

        const data = await response.json().catch(() => ({}));
        if (!response.ok) {
            throw new Error(data.message || 'Yeu cau that bai');
        }
        return data;
    }

    function showToast(message, type) {
        if (!toastMessage) {
            return;
        }
        toastMessage.textContent = message;
        toastMessage.className = `doctor-toast show ${type || 'success'}`;
        window.clearTimeout(showToast.timer);
        showToast.timer = window.setTimeout(() => {
            toastMessage.className = 'doctor-toast';
        }, 3200);
    }

    function renderOptions(selectId, items, placeholder, labelKey) {
        const select = document.getElementById(selectId);
        const currentValue = select.value;
        select.innerHTML = `<option value="">${placeholder}</option>`;
        items.forEach(item => {
            const option = document.createElement('option');
            option.value = item.id;
            option.textContent = item[labelKey || 'name'];
            select.appendChild(option);
        });
        if ([...select.options].some(option => option.value === currentValue)) {
            select.value = currentValue;
        }
    }

    function renderUserOptions(users, selectedValue) {
        const select = document.getElementById('doctorUserId');
        select.innerHTML = '<option value="">Chon tai khoan nguoi dung</option>';
        users.forEach(user => {
            const option = document.createElement('option');
            option.value = user.id;
            option.textContent = `${user.fullName || 'Khong ten'}${user.role ? ' - ' + user.role : ''}`;
            select.appendChild(option);
        });
        if (selectedValue) {
            select.value = String(selectedValue);
        }
    }

    function statusBadge(status) {
        if (status === 'ACTIVE') {
            return '<span class="status-badge status-success">ACTIVE</span>';
        }
        if (status === 'INACTIVE') {
            return '<span class="status-badge status-warning">INACTIVE</span>';
        }
        return '<span class="status-badge status-danger">LOCKED</span>';
    }

    function workingBadge(text) {
        if (text === 'Dang lam viec') {
            return '<span class="status-badge status-success">Dang lam viec</span>';
        }
        if (text === 'Co lich hom nay') {
            return '<span class="status-badge status-info">Co lich hom nay</span>';
        }
        if (text === 'Tam ngung' || text === 'Bi khoa') {
            return `<span class="status-badge status-danger">${text}</span>`;
        }
        return `<span class="status-badge status-warning">${text || 'Chua xep lich'}</span>`;
    }

    function updateMetrics() {
        document.getElementById('metricTotal').textContent = state.doctors.length;
        document.getElementById('metricActive').textContent = state.doctors.filter(item => item.status === 'ACTIVE').length;
        document.getElementById('metricToday').textContent = state.doctors.filter(item => item.todayScheduleCount > 0).length;
        document.getElementById('metricWorking').textContent = state.doctors.filter(item => item.workingStatus === 'Dang lam viec').length;
    }

    function buildInitials(name) {
        if (!name) {
            return 'BS';
        }
        return name
            .split(' ')
            .filter(Boolean)
            .slice(-2)
            .map(part => part.charAt(0).toUpperCase())
            .join('');
    }

    function renderDoctors() {
        if (!state.doctors.length) {
            doctorTableBody.innerHTML = '<tr><td colspan="6" class="empty-state">Khong co bac si phu hop voi bo loc hien tai.</td></tr>';
            updateMetrics();
            return;
        }

        doctorTableBody.innerHTML = state.doctors.map(doctor => `
            <tr class="${state.selectedDoctorId === doctor.id ? 'selected-row' : ''}">
                <td>
                    <div class="patient-cell">
                        <div class="patient-avatar bg-1">${buildInitials(doctor.fullName)}</div>
                        <div>
                            <div class="patient-name">${doctor.fullName || ''}</div>
                            <div class="patient-id">${doctor.licenseNumber || ''}</div>
                        </div>
                    </div>
                </td>
                <td>
                    <div>${doctor.specialtyName || doctor.specialization || '-'}</div>
                    <div class="patient-id">${doctor.experienceYears ?? 0} nam kinh nghiem</div>
                </td>
                <td>${doctor.departmentName || '-'}</td>
                <td>
                    ${statusBadge(doctor.status)}
                    <div style="margin-top:.4rem;">${workingBadge(doctor.workingStatus)}</div>
                </td>
                <td>${doctor.todayScheduleCount}</td>
                <td>
                    <div class="table-actions">
                        <button type="button" class="table-action-btn" data-action="edit" data-id="${doctor.id}" title="Sua ho so">
                            <i class="bi bi-pencil-square"></i>
                        </button>
                        <button type="button" class="table-action-btn" data-action="schedule" data-id="${doctor.id}" title="Quan ly lich">
                            <i class="bi bi-calendar-week"></i>
                        </button>
                        <button type="button" class="table-action-btn danger" data-action="delete" data-id="${doctor.id}" title="Xoa bac si">
                            <i class="bi bi-trash"></i>
                        </button>
                    </div>
                </td>
            </tr>
        `).join('');

        updateMetrics();
    }

    function renderSchedules(schedules) {
        if (!state.selectedDoctorId) {
            scheduleTableBody.innerHTML = '<tr><td colspan="4" class="empty-state">Chua chon bac si.</td></tr>';
            return;
        }
        if (!schedules.length) {
            scheduleTableBody.innerHTML = '<tr><td colspan="4" class="empty-state">Bac si nay chua co lich lam viec.</td></tr>';
            return;
        }

        scheduleTableBody.innerHTML = schedules.map(schedule => `
            <tr>
                <td>${schedule.workDate || ''}</td>
                <td>${schedule.startTime || ''} - ${schedule.endTime || ''}</td>
                <td>${schedule.status === 'AVAILABLE'
                    ? '<span class="status-badge status-success">AVAILABLE</span>'
                    : '<span class="status-badge status-warning">UNAVAILABLE</span>'}</td>
                <td>
                    <div class="table-actions">
                        <button type="button" class="table-action-btn" data-schedule-action="edit" data-id="${schedule.id}" title="Sua lich">
                            <i class="bi bi-pencil"></i>
                        </button>
                        <button type="button" class="table-action-btn danger" data-schedule-action="delete" data-id="${schedule.id}" title="Xoa lich">
                            <i class="bi bi-trash"></i>
                        </button>
                    </div>
                </td>
            </tr>
        `).join('');
    }

    function collectDoctorPayload() {
        return {
            userId: Number(document.getElementById('doctorUserId').value),
            fullName: document.getElementById('doctorFullName').value.trim(),
            email: document.getElementById('doctorEmail').value.trim(),
            phone: document.getElementById('doctorPhone').value.trim(),
            licenseNumber: document.getElementById('doctorLicenseNumber').value.trim(),
            experienceYears: Number(document.getElementById('doctorExperienceYears').value),
            status: document.getElementById('doctorStatus').value,
            specialtyId: Number(document.getElementById('doctorSpecialtyId').value),
            departmentId: Number(document.getElementById('doctorDepartmentId').value)
        };
    }

    function collectSchedulePayload() {
        return {
            workDate: document.getElementById('scheduleWorkDate').value,
            startTime: document.getElementById('scheduleStartTime').value,
            endTime: document.getElementById('scheduleEndTime').value,
            status: document.getElementById('scheduleStatus').value
        };
    }

    async function loadMetadata() {
        const [specialtiesResult, departmentsResult, usersResult] = await Promise.allSettled([
            fetchJson('/api/doctors/specialties'),
            fetchJson('/api/doctors/departments'),
            fetchJson('/api/doctors/users/available')
        ]);

        state.specialties = specialtiesResult.status === 'fulfilled' ? specialtiesResult.value : [];
        state.departments = departmentsResult.status === 'fulfilled' ? departmentsResult.value : [];
        state.users = usersResult.status === 'fulfilled' ? usersResult.value : [];

        renderOptions('doctorSpecialtyFilter', state.specialties, 'Tat ca chuyen khoa');
        renderOptions('doctorDepartmentFilter', state.departments, 'Tat ca khoa/phong');
        renderOptions('doctorSpecialtyId', state.specialties, 'Chon chuyen khoa');
        renderOptions('doctorDepartmentId', state.departments, 'Chon khoa/phong');
        renderUserOptions(state.users);

        if (specialtiesResult.status === 'rejected') {
            showToast('Khong tai duoc danh muc chuyen khoa', 'error');
        }
        if (departmentsResult.status === 'rejected') {
            showToast('Khong tai duoc danh muc khoa phong', 'error');
        }
        if (usersResult.status === 'rejected') {
            showToast('Khong tai duoc danh sach tai khoan', 'error');
        }
    }

    async function loadDoctors() {
        const params = new URLSearchParams();
        const keyword = document.getElementById('doctorKeywordFilter').value.trim();
        const specialtyId = document.getElementById('doctorSpecialtyFilter').value;
        const departmentId = document.getElementById('doctorDepartmentFilter').value;
        const status = document.getElementById('doctorStatusFilter').value;
        const availableToday = document.getElementById('doctorAvailableTodayFilter').checked;

        if (keyword) params.set('keyword', keyword);
        if (specialtyId) params.set('specialtyId', specialtyId);
        if (departmentId) params.set('departmentId', departmentId);
        if (status) params.set('status', status);
        if (availableToday) params.set('availableToday', 'true');

        state.doctors = await fetchJson(`/api/doctors/findAll?${params.toString()}`);
        renderDoctors();
    }

    async function loadUsersForDoctor(doctorId, selectedUserId) {
        const query = doctorId ? `?doctorId=${doctorId}` : '';
        state.users = await fetchJson(`/api/doctors/users/available${query}`);
        renderUserOptions(state.users, selectedUserId);
    }

    async function selectDoctor(id) {
        state.selectedDoctorId = id;
        renderDoctors();

        const doctor = state.doctors.find(item => item.id === id);
        if (!doctor) {
            return;
        }

        scheduleDoctorInfo.textContent = `${doctor.fullName} | ${doctor.specialtyName || doctor.specialization || '-'} | ${doctor.workingStatus}`;
        const schedules = await fetchJson(`/api/doctors/${id}/schedules`);
        renderSchedules(schedules);
        document.getElementById('doctorScheduleId').value = '';
    }

    async function editDoctor(id) {
        const doctor = await fetchJson(`/api/doctors/${id}`);
        document.getElementById('doctorId').value = doctor.id;
        document.getElementById('doctorFullName').value = doctor.fullName || '';
        document.getElementById('doctorEmail').value = doctor.email || '';
        document.getElementById('doctorPhone').value = doctor.phone || '';
        document.getElementById('doctorLicenseNumber').value = doctor.licenseNumber || '';
        document.getElementById('doctorExperienceYears').value = doctor.experienceYears ?? 0;
        document.getElementById('doctorStatus').value = doctor.status || 'ACTIVE';
        document.getElementById('doctorSpecialtyId').value = doctor.specialtyId || '';
        document.getElementById('doctorDepartmentId').value = doctor.departmentId || '';
        await loadUsersForDoctor(doctor.id, doctor.userId);
        await selectDoctor(doctor.id);
    }

    async function removeDoctor(id) {
        if (!confirm('Xoa ho so bac si nay? He thong se chan neu bac si da co lich lam viec, lich hen hoac benh an.')) {
            return;
        }
        await fetchJson(`/api/doctors/${id}`, { method: 'DELETE' });
        showToast('Da xoa ho so bac si');
        if (state.selectedDoctorId === id) {
            state.selectedDoctorId = null;
            scheduleDoctorInfo.textContent = 'Chon mot bac si trong bang de quan ly lich lam viec va xem trang thai hoat dong.';
            renderSchedules([]);
        }
        resetDoctorForm();
        await loadUsersForDoctor();
        await loadDoctors();
    }

    async function editSchedule(scheduleId) {
        if (!state.selectedDoctorId) {
            showToast('Hay chon bac si truoc khi sua lich', 'error');
            return;
        }
        const schedules = await fetchJson(`/api/doctors/${state.selectedDoctorId}/schedules`);
        const schedule = schedules.find(item => item.id === scheduleId);
        if (!schedule) {
            return;
        }
        document.getElementById('doctorScheduleId').value = schedule.id;
        document.getElementById('scheduleWorkDate').value = schedule.workDate || '';
        document.getElementById('scheduleStartTime').value = schedule.startTime || '';
        document.getElementById('scheduleEndTime').value = schedule.endTime || '';
        document.getElementById('scheduleStatus').value = schedule.status || 'AVAILABLE';
    }

    async function removeSchedule(scheduleId) {
        if (!state.selectedDoctorId) {
            showToast('Hay chon bac si truoc khi xoa lich', 'error');
            return;
        }
        if (!confirm('Xoa lich lam viec nay?')) {
            return;
        }
        await fetchJson(`/api/doctors/${state.selectedDoctorId}/schedules/${scheduleId}`, { method: 'DELETE' });
        showToast('Da xoa lich lam viec');
        resetScheduleForm();
        await selectDoctor(state.selectedDoctorId);
        await loadDoctors();
    }

    function resetDoctorForm() {
        document.getElementById('doctorForm').reset();
        document.getElementById('doctorId').value = '';
        renderOptions('doctorSpecialtyId', state.specialties, 'Chon chuyen khoa');
        renderOptions('doctorDepartmentId', state.departments, 'Chon khoa/phong');
        renderUserOptions(state.users);
        document.getElementById('doctorStatus').value = 'ACTIVE';
    }

    function resetScheduleForm() {
        document.getElementById('doctorScheduleForm').reset();
        document.getElementById('doctorScheduleId').value = '';
        document.getElementById('scheduleStatus').value = 'AVAILABLE';
    }

    doctorTableBody.addEventListener('click', async event => {
        const button = event.target.closest('button[data-action]');
        if (!button) {
            return;
        }
        const id = Number(button.dataset.id);
        try {
            if (button.dataset.action === 'edit') {
                await editDoctor(id);
            }
            if (button.dataset.action === 'schedule') {
                await selectDoctor(id);
            }
            if (button.dataset.action === 'delete') {
                await removeDoctor(id);
            }
        } catch (error) {
            showToast(error.message, 'error');
        }
    });

    scheduleTableBody.addEventListener('click', async event => {
        const button = event.target.closest('button[data-schedule-action]');
        if (!button) {
            return;
        }
        const id = Number(button.dataset.id);
        try {
            if (button.dataset.scheduleAction === 'edit') {
                await editSchedule(id);
            }
            if (button.dataset.scheduleAction === 'delete') {
                await removeSchedule(id);
            }
        } catch (error) {
            showToast(error.message, 'error');
        }
    });

    document.getElementById('doctorFilterForm').addEventListener('submit', async event => {
        event.preventDefault();
        try {
            await loadDoctors();
        } catch (error) {
            showToast(error.message, 'error');
        }
    });

    document.getElementById('doctorResetFilterBtn').addEventListener('click', async () => {
        document.getElementById('doctorFilterForm').reset();
        try {
            await loadDoctors();
        } catch (error) {
            showToast(error.message, 'error');
        }
    });

    document.getElementById('doctorForm').addEventListener('submit', async event => {
        event.preventDefault();
        const doctorId = document.getElementById('doctorId').value;
        const payload = collectDoctorPayload();
        try {
            if (doctorId) {
                await fetchJson(`/api/doctors/${doctorId}`, {
                    method: 'PUT',
                    body: JSON.stringify(payload)
                });
                showToast('Da cap nhat ho so bac si');
            } else {
                await fetchJson('/api/doctors', {
                    method: 'POST',
                    body: JSON.stringify(payload)
                });
                showToast('Da tao ho so bac si');
            }
            resetDoctorForm();
            await loadUsersForDoctor();
            await loadDoctors();
        } catch (error) {
            showToast(error.message, 'error');
        }
    });

    document.getElementById('doctorScheduleForm').addEventListener('submit', async event => {
        event.preventDefault();
        if (!state.selectedDoctorId) {
            showToast('Hay chon bac si truoc khi luu lich lam viec', 'error');
            return;
        }
        const scheduleId = document.getElementById('doctorScheduleId').value;
        const payload = collectSchedulePayload();
        try {
            if (scheduleId) {
                await fetchJson(`/api/doctors/${state.selectedDoctorId}/schedules/${scheduleId}`, {
                    method: 'PUT',
                    body: JSON.stringify(payload)
                });
                showToast('Da cap nhat lich lam viec');
            } else {
                await fetchJson(`/api/doctors/${state.selectedDoctorId}/schedules`, {
                    method: 'POST',
                    body: JSON.stringify(payload)
                });
                showToast('Da them lich lam viec');
            }
            resetScheduleForm();
            await selectDoctor(state.selectedDoctorId);
            await loadDoctors();
        } catch (error) {
            showToast(error.message, 'error');
        }
    });

    document.getElementById('doctorResetFormBtn').addEventListener('click', () => {
        resetDoctorForm();
        loadUsersForDoctor().catch(error => showToast(error.message, 'error'));
    });

    document.getElementById('doctorResetScheduleBtn').addEventListener('click', resetScheduleForm);

    document.getElementById('doctorUserId').addEventListener('change', event => {
        const user = state.users.find(item => String(item.id) === event.target.value);
        if (!user) {
            return;
        }
        document.getElementById('doctorFullName').value = user.fullName || '';
        document.getElementById('doctorEmail').value = user.email || '';
        document.getElementById('doctorPhone').value = user.phone || '';
    });

    loadMetadata()
        .then(loadDoctors)
        .then(() => {
            document.getElementById('doctorStatus').value = 'ACTIVE';
            document.getElementById('scheduleStatus').value = 'AVAILABLE';
        })
        .catch(error => showToast(error.message, 'error'));
})();
