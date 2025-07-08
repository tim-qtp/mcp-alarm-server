// 告警管理系统 JavaScript
const API_BASE_URL = '/api/alerts';

// 全局变量
let currentPage = 1;
let pageSize = 10;
let totalPages = 0;
let selectedAlerts = new Set();
let charts = {};

// 页面加载完成后初始化
document.addEventListener('DOMContentLoaded', function() {
    initializePage();
    loadAlerts();
    loadStatistics();
    updateCurrentTime();
    setInterval(updateCurrentTime, 1000);
});

// 初始化页面
function initializePage() {
    // 初始化时间选择器
    $('#searchTimeRange').daterangepicker({
        timePicker: true,
        timePickerIncrement: 30,
        locale: {
            format: 'YYYY-MM-DD HH:mm:ss'
        }
    });

    // 初始化表单验证
    initializeFormValidation();
}

// 更新当前时间
function updateCurrentTime() {
    const now = new Date();
    document.getElementById('currentTime').textContent = now.toLocaleString('zh-CN');
}

// 加载告警列表
async function loadAlerts(page = 1) {
    showLoading(true);
    currentPage = page;
    
    try {
        const params = buildSearchParams();
        const response = await fetch(`${API_BASE_URL}/search?${params}`);
        const data = await response.json();
        
        if (response.ok) {
            renderAlertTable(data.data);
            renderPagination(data.total, data.pageSize, data.pageNum);
            updateAlertCount(data.total);
        } else {
            showError('加载告警列表失败');
        }
    } catch (error) {
        console.error('加载告警列表错误:', error);
        showError('网络错误，请检查连接');
    } finally {
        showLoading(false);
    }
}

// 构建搜索参数
function buildSearchParams() {
    const params = new URLSearchParams();
    
    // 搜索条件
    const caseExecId = document.getElementById('searchCaseExecId').value;
    const alarmLevel = document.getElementById('searchAlarmLevel').value;
    const alarmType = document.getElementById('searchAlarmType').value;
    const status = document.getElementById('searchStatus').value;
    const company = document.getElementById('searchCompany').value;
    const systemName = document.getElementById('searchSystemName').value;
    const taskName = document.getElementById('searchTaskName').value;
    const host = document.getElementById('searchHost').value;
    const sortField = document.getElementById('searchSortField').value;
    const sortOrder = document.getElementById('searchSortOrder').value;
    
    if (caseExecId) params.append('caseExecId', caseExecId);
    if (alarmLevel) params.append('alarmLevel', alarmLevel);
    if (alarmType) params.append('alarmType', alarmType);
    if (status !== '') params.append('status', status);
    if (company) params.append('company', company);
    if (systemName) params.append('systemName', systemName);
    if (taskName) params.append('taskName', taskName);
    if (host) params.append('host', host);
    
    // 时间范围
    const timeRange = $('#searchTimeRange').data('daterangepicker');
    if (timeRange && timeRange.startDate && timeRange.endDate) {
        params.append('beginTime', timeRange.startDate.format('YYYY-MM-DD HH:mm:ss'));
        params.append('endTime', timeRange.endDate.format('YYYY-MM-DD HH:mm:ss'));
    }
    
    // 分页和排序
    params.append('pageNum', currentPage);
    params.append('pageSize', pageSize);
    params.append('sortField', sortField);
    params.append('sortOrder', sortOrder);
    
    return params.toString();
}

// 渲染告警表格
function renderAlertTable(alerts) {
    const tbody = document.getElementById('alertTableBody');
    tbody.innerHTML = '';
    
    if (alerts.length === 0) {
        tbody.innerHTML = '<tr><td colspan="10" class="text-center text-muted">暂无数据</td></tr>';
        return;
    }
    
    alerts.forEach(alert => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>
                <input type="checkbox" class="form-check-input alert-checkbox" 
                       value="${alert.id}" onchange="toggleAlertSelection('${alert.id}')">
            </td>
            <td>${alert.caseExecId || '-'}</td>
            <td>
                <span class="badge alert-level-${alert.alarmLevel}">
                    ${getLevelText(alert.alarmLevel)}
                </span>
            </td>
            <td>${getTypeText(alert.alarmType)}</td>
            <td>
                <span class="status-${getStatusClass(alert.status)}">
                    ${getStatusText(alert.status)}
                </span>
            </td>
            <td>${alert.company || '-'}</td>
            <td>${alert.systemName || '-'}</td>
            <td>${alert.taskName || '-'}</td>
            <td title="${alert.failReason || ''}">
                ${truncateText(alert.failReason, 50)}
            </td>
            <td>${formatDateTime(alert.endTime)}</td>
            <td>
                <div class="btn-group btn-group-sm">
                    <button class="btn btn-outline-primary btn-action" onclick="viewAlert('${alert.id}')" title="查看">
                        <i class="bi bi-eye"></i>
                    </button>
                    <button class="btn btn-outline-warning btn-action" onclick="editAlert('${alert.id}')" title="编辑">
                        <i class="bi bi-pencil"></i>
                    </button>
                    ${alert.status !== 1 ? 
                        `<button class="btn btn-outline-success btn-action" onclick="resolveAlert('${alert.id}')" title="解决">
                            <i class="bi bi-check-circle"></i>
                        </button>` : ''
                    }
                    ${alert.alarmLevel !== 'critical' ? 
                        `<button class="btn btn-outline-danger btn-action" onclick="escalateAlert('${alert.id}')" title="升级">
                            <i class="bi bi-arrow-up-circle"></i>
                        </button>` : ''
                    }
                    <button class="btn btn-outline-danger btn-action" onclick="deleteAlert('${alert.id}')" title="删除">
                        <i class="bi bi-trash"></i>
                    </button>
                </div>
            </td>
        `;
        tbody.appendChild(row);
    });
}

// 渲染分页
function renderPagination(total, pageSize, currentPage) {
    const pagination = document.getElementById('pagination');
    const totalPages = Math.ceil(total / pageSize);
    
    pagination.innerHTML = '';
    
    if (totalPages <= 1) return;
    
    // 上一页
    const prevLi = document.createElement('li');
    prevLi.className = `page-item ${currentPage === 1 ? 'disabled' : ''}`;
    prevLi.innerHTML = `<a class="page-link" href="#" onclick="loadAlerts(${currentPage - 1})">上一页</a>`;
    pagination.appendChild(prevLi);
    
    // 页码
    const startPage = Math.max(1, currentPage - 2);
    const endPage = Math.min(totalPages, currentPage + 2);
    
    for (let i = startPage; i <= endPage; i++) {
        const li = document.createElement('li');
        li.className = `page-item ${i === currentPage ? 'active' : ''}`;
        li.innerHTML = `<a class="page-link" href="#" onclick="loadAlerts(${i})">${i}</a>`;
        pagination.appendChild(li);
    }
    
    // 下一页
    const nextLi = document.createElement('li');
    nextLi.className = `page-item ${currentPage === totalPages ? 'disabled' : ''}`;
    nextLi.innerHTML = `<a class="page-link" href="#" onclick="loadAlerts(${currentPage + 1})">下一页</a>`;
    pagination.appendChild(nextLi);
}

// 加载统计信息
async function loadStatistics() {
    try {
        const response = await fetch(`${API_BASE_URL}/statistics`);
        const data = await response.json();
        
        if (response.ok) {
            updateStatisticsDisplay(data);
        }
    } catch (error) {
        console.error('加载统计信息错误:', error);
    }
}

// 更新统计显示
function updateStatisticsDisplay(stats) {
    document.getElementById('totalAlerts').textContent = stats.totalCount || 0;
    document.getElementById('activeAlerts').textContent = stats.statusStats?.[0] || 0;
    document.getElementById('resolvedAlerts').textContent = stats.statusStats?.[1] || 0;
    document.getElementById('criticalAlerts').textContent = stats.levelStats?.critical || 0;
    document.getElementById('warningAlerts').textContent = stats.levelStats?.warning || 0;
    document.getElementById('infoAlerts').textContent = stats.levelStats?.info || 0;
}

// 搜索告警
function searchAlerts() {
    loadAlerts(1);
}

// 重置搜索
function resetSearch() {
    document.getElementById('searchCaseExecId').value = '';
    document.getElementById('searchAlarmLevel').value = '';
    document.getElementById('searchAlarmType').value = '';
    document.getElementById('searchStatus').value = '';
    document.getElementById('searchCompany').value = '';
    document.getElementById('searchSystemName').value = '';
    document.getElementById('searchTaskName').value = '';
    document.getElementById('searchHost').value = '';
    $('#searchTimeRange').data('daterangepicker').setStartDate(moment().subtract(7, 'days'));
    $('#searchTimeRange').data('daterangepicker').setEndDate(moment());
    loadAlerts(1);
}

// 刷新数据
function refreshData() {
    loadAlerts(currentPage);
    loadStatistics();
}

// 查看告警详情
async function viewAlert(id) {
    try {
        const response = await fetch(`${API_BASE_URL}/${id}`);
        const alert = await response.json();
        
        if (response.ok) {
            showAlertDetail(alert);
        } else {
            showError('获取告警详情失败');
        }
    } catch (error) {
        console.error('获取告警详情错误:', error);
        showError('网络错误');
    }
}

// 显示告警详情
function showAlertDetail(alert) {
    const content = document.getElementById('alertDetailContent');
    content.innerHTML = `
        <div class="row">
            <div class="col-md-6">
                <h6>基本信息</h6>
                <table class="table table-sm">
                    <tr><td>告警ID:</td><td>${alert.caseExecId || '-'}</td></tr>
                    <tr><td>告警级别:</td><td><span class="badge alert-level-${alert.alarmLevel}">${getLevelText(alert.alarmLevel)}</span></td></tr>
                    <tr><td>告警类型:</td><td>${getTypeText(alert.alarmType)}</td></tr>
                    <tr><td>状态:</td><td><span class="status-${getStatusClass(alert.status)}">${getStatusText(alert.status)}</span></td></tr>
                    <tr><td>所属单位:</td><td>${alert.company || '-'}</td></tr>
                    <tr><td>系统名称:</td><td>${alert.systemName || '-'}</td></tr>
                    <tr><td>模块名称:</td><td>${alert.taskName || '-'}</td></tr>
                    <tr><td>主机IP:</td><td>${alert.host || '-'}</td></tr>
                </table>
            </div>
            <div class="col-md-6">
                <h6>时间信息</h6>
                <table class="table table-sm">
                    <tr><td>告警时间:</td><td>${formatDateTime(alert.endTime)}</td></tr>
                    <tr><td>开始时间:</td><td>${formatDateTime(alert.beginTime)}</td></tr>
                    <tr><td>恢复时间:</td><td>${formatDateTime(alert.recoverTime)}</td></tr>
                    <tr><td>平均时长:</td><td>${alert.aveTime || '-'}</td></tr>
                </table>
            </div>
        </div>
        <div class="row mt-3">
            <div class="col-12">
                <h6>告警信息</h6>
                <div class="alert alert-info">${alert.failReason || '无'}</div>
            </div>
        </div>
        <div class="row mt-3">
            <div class="col-md-6">
                <h6>其他信息</h6>
                <table class="table table-sm">
                    <tr><td>告警来源:</td><td>${alert.layerName || '-'}</td></tr>
                    <tr><td>所属网域:</td><td>${alert.regionName || '-'}</td></tr>
                    <tr><td>是否恢复:</td><td>${alert.isRecover ? '是' : '否'}</td></tr>
                    <tr><td>是否响应:</td><td>${alert.isReply === 1 ? '是' : '否'}</td></tr>
                </table>
            </div>
            <div class="col-md-6">
                <h6>探测信息</h6>
                <table class="table table-sm">
                    <tr><td>接口探测报文:</td><td>${truncateText(alert.response, 30) || '-'}</td></tr>
                    <tr><td>UI探测报文:</td><td>${truncateText(alert.actualValue, 30) || '-'}</td></tr>
                </table>
            </div>
        </div>
    `;
    
    const modal = new bootstrap.Modal(document.getElementById('alertDetailModal'));
    modal.show();
}

// 编辑告警
async function editAlert(id) {
    try {
        const response = await fetch(`${API_BASE_URL}/${id}`);
        const alert = await response.json();
        
        if (response.ok) {
            fillAlertForm(alert);
            document.getElementById('alertFormTitle').textContent = '编辑告警';
            const modal = new bootstrap.Modal(document.getElementById('alertFormModal'));
            modal.show();
        } else {
            showError('获取告警信息失败');
        }
    } catch (error) {
        console.error('获取告警信息错误:', error);
        showError('网络错误');
    }
}

// 填充告警表单
function fillAlertForm(alert) {
    document.getElementById('formCaseExecId').value = alert.caseExecId || '';
    document.getElementById('formAlarmLevel').value = alert.alarmLevel || 'info';
    document.getElementById('formAlarmType').value = alert.alarmType || 'host';
    document.getElementById('formStatus').value = alert.status || 0;
    document.getElementById('formCompany').value = alert.company || '';
    document.getElementById('formSystemName').value = alert.systemName || '';
    document.getElementById('formTaskName').value = alert.taskName || '';
    document.getElementById('formHost').value = alert.host || '';
    document.getElementById('formFailReason').value = alert.failReason || '';
    document.getElementById('formIsRecover').value = alert.isRecover ? 'true' : 'false';
    
    if (alert.endTime) {
        const date = new Date(alert.endTime);
        document.getElementById('formEndTime').value = date.toISOString().slice(0, 16);
    }
}

// 显示新建告警模态框
function showCreateModal() {
    clearAlertForm();
    document.getElementById('alertFormTitle').textContent = '新建告警';
    const modal = new bootstrap.Modal(document.getElementById('alertFormModal'));
    modal.show();
}

// 清空告警表单
function clearAlertForm() {
    document.getElementById('alertForm').reset();
    document.getElementById('formEndTime').value = '';
}

// 保存告警
async function saveAlert() {
    if (!validateAlertForm()) {
        return;
    }
    
    const alertData = buildAlertData();
    const isEdit = document.getElementById('alertFormTitle').textContent === '编辑告警';
    
    try {
        const url = isEdit ? `${API_BASE_URL}/${alertData.id}` : API_BASE_URL;
        const method = isEdit ? 'PUT' : 'POST';
        
        const response = await fetch(url, {
            method: method,
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(alertData)
        });
        
        if (response.ok) {
            showSuccess(isEdit ? '告警更新成功' : '告警创建成功');
            bootstrap.Modal.getInstance(document.getElementById('alertFormModal')).hide();
            loadAlerts(currentPage);
        } else {
            showError('保存失败');
        }
    } catch (error) {
        console.error('保存告警错误:', error);
        showError('网络错误');
    }
}

// 构建告警数据
function buildAlertData() {
    return {
        caseExecId: document.getElementById('formCaseExecId').value,
        alarmLevel: document.getElementById('formAlarmLevel').value,
        alarmType: document.getElementById('formAlarmType').value,
        status: parseInt(document.getElementById('formStatus').value),
        company: document.getElementById('formCompany').value,
        systemName: document.getElementById('formSystemName').value,
        taskName: document.getElementById('formTaskName').value,
        host: document.getElementById('formHost').value,
        failReason: document.getElementById('formFailReason').value,
        isRecover: document.getElementById('formIsRecover').value === 'true',
        endTime: document.getElementById('formEndTime').value ? new Date(document.getElementById('formEndTime').value) : new Date()
    };
}

// 验证告警表单
function validateAlertForm() {
    const form = document.getElementById('alertForm');
    return form.checkValidity();
}

// 解决告警
async function resolveAlert(id) {
    if (!confirm('确定要解决这个告警吗？')) {
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE_URL}/${id}/resolve`, {
            method: 'PUT'
        });
        
        if (response.ok) {
            showSuccess('告警已解决');
            loadAlerts(currentPage);
            loadStatistics();
        } else {
            showError('解决告警失败');
        }
    } catch (error) {
        console.error('解决告警错误:', error);
        showError('网络错误');
    }
}

// 升级告警
async function escalateAlert(id) {
    if (!confirm('确定要升级这个告警吗？')) {
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE_URL}/${id}/escalate`, {
            method: 'PUT'
        });
        
        if (response.ok) {
            showSuccess('告警已升级');
            loadAlerts(currentPage);
            loadStatistics();
        } else {
            const error = await response.text();
            showError(error || '升级告警失败');
        }
    } catch (error) {
        console.error('升级告警错误:', error);
        showError('网络错误');
    }
}

// 删除告警
async function deleteAlert(id) {
    if (!confirm('确定要删除这个告警吗？此操作不可恢复！')) {
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE_URL}/${id}`, {
            method: 'DELETE'
        });
        
        if (response.ok) {
            showSuccess('告警已删除');
            loadAlerts(currentPage);
            loadStatistics();
        } else {
            showError('删除告警失败');
        }
    } catch (error) {
        console.error('删除告警错误:', error);
        showError('网络错误');
    }
}

// 批量解决告警
async function batchResolve() {
    const selectedIds = Array.from(selectedAlerts);
    if (selectedIds.length === 0) {
        showWarning('请先选择要解决的告警');
        return;
    }
    
    if (!confirm(`确定要解决选中的 ${selectedIds.length} 个告警吗？`)) {
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE_URL}/batch/status`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                ids: selectedIds,
                status: 1
            })
        });
        
        if (response.ok) {
            const result = await response.json();
            showSuccess(`成功解决 ${result.updatedCount} 个告警`);
            selectedAlerts.clear();
            updateSelectAllCheckbox();
            loadAlerts(currentPage);
            loadStatistics();
        } else {
            showError('批量解决失败');
        }
    } catch (error) {
        console.error('批量解决告警错误:', error);
        showError('网络错误');
    }
}

// 批量删除告警
async function batchDelete() {
    const selectedIds = Array.from(selectedAlerts);
    if (selectedIds.length === 0) {
        showWarning('请先选择要删除的告警');
        return;
    }
    
    if (!confirm(`确定要删除选中的 ${selectedIds.length} 个告警吗？此操作不可恢复！`)) {
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE_URL}/batch`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(selectedIds)
        });
        
        if (response.ok) {
            const result = await response.json();
            showSuccess(`成功删除 ${result.deletedCount} 个告警`);
            selectedAlerts.clear();
            updateSelectAllCheckbox();
            loadAlerts(currentPage);
            loadStatistics();
        } else {
            showError('批量删除失败');
        }
    } catch (error) {
        console.error('批量删除告警错误:', error);
        showError('网络错误');
    }
}

// 切换告警选择
function toggleAlertSelection(id) {
    if (selectedAlerts.has(id)) {
        selectedAlerts.delete(id);
    } else {
        selectedAlerts.add(id);
    }
    updateSelectAllCheckbox();
}

// 切换全选
function toggleSelectAll() {
    const selectAll = document.getElementById('selectAll');
    const checkboxes = document.querySelectorAll('.alert-checkbox');
    
    checkboxes.forEach(checkbox => {
        checkbox.checked = selectAll.checked;
        if (selectAll.checked) {
            selectedAlerts.add(checkbox.value);
        } else {
            selectedAlerts.delete(checkbox.value);
        }
    });
}

// 更新全选复选框状态
function updateSelectAllCheckbox() {
    const selectAll = document.getElementById('selectAll');
    const checkboxes = document.querySelectorAll('.alert-checkbox');
    const checkedCount = selectedAlerts.size;
    
    selectAll.checked = checkedCount === checkboxes.length && checkboxes.length > 0;
    selectAll.indeterminate = checkedCount > 0 && checkedCount < checkboxes.length;
}

// 显示统计图表
async function showStatistics() {
    try {
        const response = await fetch(`${API_BASE_URL}/statistics`);
        const data = await response.json();
        
        if (response.ok) {
            renderStatisticsCharts(data);
            const modal = new bootstrap.Modal(document.getElementById('statisticsModal'));
            modal.show();
        }
    } catch (error) {
        console.error('加载统计图表错误:', error);
        showError('加载统计图表失败');
    }
}

// 渲染统计图表
function renderStatisticsCharts(data) {
    // 销毁现有图表
    Object.values(charts).forEach(chart => {
        if (chart) chart.destroy();
    });
    charts = {};
    
    // 告警级别图表
    const levelCtx = document.getElementById('levelChart').getContext('2d');
    charts.levelChart = new Chart(levelCtx, {
        type: 'doughnut',
        data: {
            labels: ['信息', '警告', '严重'],
            datasets: [{
                data: [
                    data.levelStats?.info || 0,
                    data.levelStats?.warning || 0,
                    data.levelStats?.critical || 0
                ],
                backgroundColor: ['#0dcaf0', '#ffc107', '#dc3545']
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                title: {
                    display: true,
                    text: '告警级别分布'
                }
            }
        }
    });
    
    // 告警状态图表
    const statusCtx = document.getElementById('statusChart').getContext('2d');
    charts.statusChart = new Chart(statusCtx, {
        type: 'pie',
        data: {
            labels: ['活跃', '已解决', '待处理'],
            datasets: [{
                data: [
                    data.statusStats?.[0] || 0,
                    data.statusStats?.[1] || 0,
                    data.statusStats?.[2] || 0
                ],
                backgroundColor: ['#dc3545', '#198754', '#ffc107']
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                title: {
                    display: true,
                    text: '告警状态分布'
                }
            }
        }
    });
    
    // 告警类型图表
    const typeCtx = document.getElementById('typeChart').getContext('2d');
    charts.typeChart = new Chart(typeCtx, {
        type: 'bar',
        data: {
            labels: ['主机', '业务'],
            datasets: [{
                label: '告警数量',
                data: [
                    data.typeStats?.host || 0,
                    data.typeStats?.business || 0
                ],
                backgroundColor: ['#0d6efd', '#6f42c1']
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                title: {
                    display: true,
                    text: '告警类型分布'
                }
            },
            scales: {
                y: {
                    beginAtZero: true
                }
            }
        }
    });
    
    // 公司分布图表
    const companyCtx = document.getElementById('companyChart').getContext('2d');
    const companyLabels = Object.keys(data.companyStats || {});
    const companyData = Object.values(data.companyStats || {});
    
    charts.companyChart = new Chart(companyCtx, {
        type: 'bar',
        data: {
            labels: companyLabels,
            datasets: [{
                label: '告警数量',
                data: companyData,
                backgroundColor: '#20c997'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                title: {
                    display: true,
                    text: '公司告警分布'
                }
            },
            scales: {
                y: {
                    beginAtZero: true
                }
            }
        }
    });
}

// 工具函数
function getLevelText(level) {
    const levelMap = {
        'info': '信息',
        'warning': '警告',
        'critical': '严重'
    };
    return levelMap[level] || level;
}

function getTypeText(type) {
    const typeMap = {
        'host': '主机',
        'business': '业务'
    };
    return typeMap[type] || type;
}

function getStatusText(status) {
    const statusMap = {
        0: '活跃',
        1: '已解决',
        2: '待处理'
    };
    return statusMap[status] || status;
}

function getStatusClass(status) {
    const classMap = {
        0: 'active',
        1: 'resolved',
        2: 'pending'
    };
    return classMap[status] || 'active';
}

function formatDateTime(dateString) {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleString('zh-CN');
}

function truncateText(text, maxLength) {
    if (!text) return '-';
    return text.length > maxLength ? text.substring(0, maxLength) + '...' : text;
}

function updateAlertCount(count) {
    document.getElementById('alertCount').textContent = count;
}

function showLoading(show) {
    document.getElementById('loading').style.display = show ? 'block' : 'none';
}

function showSuccess(message) {
    // 使用Bootstrap的toast或alert显示成功消息
    alert(message); // 简单实现，可以替换为更好的UI组件
}

function showError(message) {
    // 使用Bootstrap的toast或alert显示错误消息
    alert('错误: ' + message); // 简单实现，可以替换为更好的UI组件
}

function showWarning(message) {
    // 使用Bootstrap的toast或alert显示警告消息
    alert('警告: ' + message); // 简单实现，可以替换为更好的UI组件
}

// 初始化表单验证
function initializeFormValidation() {
    const form = document.getElementById('alertForm');
    form.addEventListener('submit', function(event) {
        event.preventDefault();
        saveAlert();
    });
}