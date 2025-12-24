<template>
  <div class="app-container">
    <!-- 异常记录管理页面（业务操作员使用） -->

    <!-- 搜索表单 -->
    <el-card shadow="hover" style="margin-bottom: 20px;">
      <el-form :model="queryParams" ref="queryForm" :inline="true" label-width="80px">
        <el-form-item label="用户ID" prop="userId">
          <el-input
            v-model="queryParams.userId"
            placeholder="请输入用户ID"
            clearable
            size="small"
            style="width: 120px"
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>
        <el-form-item label="统计周期" prop="statPeriod">
          <el-date-picker
            v-model="queryParams.statPeriod"
            type="month"
            placeholder="选择月份"
            value-format="yyyy-MM"
            size="small"
            style="width: 150px"
          />
        </el-form-item>
        <el-form-item label="异常类型" prop="ruleCode">
          <el-select v-model="queryParams.ruleCode" placeholder="选择类型" clearable size="small" style="width: 180px">
            <el-option label="凌晨用水异常" value="NIGHT_USAGE_ABNORMAL" />
            <el-option label="日均用水异常" value="HIGH_DAILY_USAGE" />
            <el-option label="周末用水异常" value="WEEKEND_USAGE_ABNORMAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="选择状态" clearable size="small" style="width: 120px">
            <el-option label="待处理" :value="0" />
            <el-option label="已处理" :value="1" />
            <el-option label="已忽略" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
          <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 统计概览 -->
    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :span="6">
        <el-card shadow="hover" class="overview-card overview-total">
          <div class="overview-content">
            <div class="overview-icon">
              <i class="el-icon-warning"></i>
            </div>
            <div class="overview-info">
              <div class="overview-label">异常总数</div>
              <div class="overview-value">{{ overviewStats.total || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="overview-card overview-pending">
          <div class="overview-content">
            <div class="overview-icon">
              <i class="el-icon-time"></i>
            </div>
            <div class="overview-info">
              <div class="overview-label">待处理</div>
              <div class="overview-value">{{ overviewStats.pending || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="overview-card overview-handled">
          <div class="overview-content">
            <div class="overview-icon">
              <i class="el-icon-circle-check"></i>
            </div>
            <div class="overview-info">
              <div class="overview-label">已处理</div>
              <div class="overview-value">{{ overviewStats.handled || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="overview-card overview-ignored">
          <div class="overview-content">
            <div class="overview-icon">
              <i class="el-icon-remove-outline"></i>
            </div>
            <div class="overview-info">
              <div class="overview-label">已忽略</div>
              <div class="overview-value">{{ overviewStats.ignored || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 操作按钮区域 -->
    <el-row :gutter="20" style="margin-bottom: 10px;">
      <el-col>
        <el-button
          type="primary"
          plain
          icon="el-icon-s-tools"
          size="medium"
          @click="showRulesDialog"
        >
          规则配置
        </el-button>
        <el-button
          type="success"
          plain
          icon="el-icon-refresh-right"
          size="medium"
          @click="handleRefresh"
        >
          刷新列表
        </el-button>
      </el-col>
    </el-row>

    <!-- 异常记录列表 -->
    <el-table
      :data="anomalyList"
      v-loading="loading"
      style="width: 100%"
      border
      stripe
      highlight-current-row
      :row-class-name="tableRowClassName">
      <el-table-column label="异常ID" prop="anomalyId" width="80" align="center" />
      <el-table-column label="用户ID" prop="userId" width="80" align="center" />
      <el-table-column label="用户名" prop="userName" width="100" align="center" show-overflow-tooltip />
      <el-table-column label="统计周期" prop="statPeriod" width="100" align="center" />
      <el-table-column label="异常类型" prop="ruleCode" width="140" align="center">
        <template slot-scope="scope">
          <el-tag :type="getAnomalyTypeTagType(scope.row.ruleCode)" size="small">
            {{ scope.row.ruleName || getAnomalyTypeName(scope.row.ruleCode) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="异常描述" prop="anomalyDescription" min-width="200" show-overflow-tooltip />
      <el-table-column label="异常值" prop="actualValue" width="100" align="center">
        <template slot-scope="scope">
          <span class="anomaly-value">{{ formatNumber(scope.row.actualValue) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="阈值" prop="thresholdValue" width="80" align="center">
        <template slot-scope="scope">
          {{ formatNumber(scope.row.thresholdValue) }}
        </template>
      </el-table-column>
      <el-table-column label="处理状态" prop="status" width="100" align="center">
        <template slot-scope="scope">
          <el-tag :type="getStatusTagType(scope.row.status)" size="small">
            {{ getStatusName(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="处理人" prop="handleBy" width="100" align="center" />
      <el-table-column label="处理时间" prop="handleTime" width="160" align="center" />
      <el-table-column label="处理意见" prop="handleRemark" width="150" show-overflow-tooltip />
      <el-table-column label="检测时间" prop="createTime" width="160" align="center" />
      <el-table-column label="操作" align="center" width="200" fixed="right">
        <template slot-scope="scope">
          <el-button
            v-if="scope.row.status === 0"
            type="text"
            icon="el-icon-check"
            size="mini"
            @click="handleProcess(scope.row, 1)"
            style="color: #67c23a;"
          >
            处理
          </el-button>
          <el-button
            v-if="scope.row.status === 0"
            type="text"
            icon="el-icon-close"
            size="mini"
            @click="handleProcess(scope.row, 2)"
            style="color: #909399;"
          >
            忽略
          </el-button>
          <el-button
            type="text"
            icon="el-icon-view"
            size="mini"
            @click="handleViewDetail(scope.row)"
          >
            详情
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <pagination
      v-show="total > 0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="fetchAnomalyList"
    />

    <!-- 处理对话框 -->
    <el-dialog
      :visible.sync="processDialogVisible"
      :title="processDialogTitle"
      width="500px"
      :close-on-click-modal="false">
      <el-form :model="processForm" label-width="100px">
        <el-form-item label="异常类型">
          <el-tag :type="getAnomalyTypeTagType(processForm.ruleCode)">
            {{ processForm.ruleName || getAnomalyTypeName(processForm.ruleCode) }}
          </el-tag>
        </el-form-item>
        <el-form-item label="异常描述">
          <span>{{ processForm.anomalyDescription }}</span>
        </el-form-item>
        <el-form-item label="异常值">
          <span class="anomaly-value">{{ formatNumber(processForm.actualValue) }}</span>
          <span style="margin-left: 10px; color: #909399;">（阈值：{{ formatNumber(processForm.thresholdValue) }}）</span>
        </el-form-item>
        <el-form-item label="处理意见" prop="handleRemark">
          <el-input
            v-model="processForm.handleRemark"
            type="textarea"
            :rows="3"
            placeholder="请输入处理意见（选填）"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="processDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitProcess">确 定</el-button>
      </div>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog
      :visible.sync="detailDialogVisible"
      title="异常详情"
      width="600px"
      :close-on-click-modal="false">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="异常ID">{{ detailData.anomalyId }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ detailData.userId }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ detailData.userName }}</el-descriptions-item>
        <el-descriptions-item label="统计周期">{{ detailData.statPeriod }}</el-descriptions-item>
        <el-descriptions-item label="异常类型" :span="2">
          <el-tag :type="getAnomalyTypeTagType(detailData.ruleCode)">
            {{ detailData.ruleName || getAnomalyTypeName(detailData.ruleCode) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="异常描述" :span="2">{{ detailData.anomalyDescription }}</el-descriptions-item>
        <el-descriptions-item label="异常值">
          <span class="anomaly-value">{{ formatNumber(detailData.actualValue) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="阈值">{{ formatNumber(detailData.thresholdValue) }}</el-descriptions-item>
        <el-descriptions-item label="处理状态">
          <el-tag :type="getStatusTagType(detailData.status)">
            {{ getStatusName(detailData.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="处理人">{{ detailData.handleBy || '-' }}</el-descriptions-item>
        <el-descriptions-item label="处理时间" :span="2">{{ detailData.handleTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="处理意见" :span="2">{{ detailData.handleRemark || '-' }}</el-descriptions-item>
        <el-descriptions-item label="检测时间" :span="2">{{ detailData.createTime }}</el-descriptions-item>
      </el-descriptions>
      <div slot="footer" class="dialog-footer">
        <el-button @click="detailDialogVisible = false">关 闭</el-button>
      </div>
    </el-dialog>

    <!-- 规则配置对话框 -->
    <el-dialog
      :visible.sync="rulesDialogVisible"
      title="异常检测规则配置"
      width="900px"
      :close-on-click-modal="false">
      <el-table :data="anomalyRules" border style="width: 100%">
        <el-table-column label="规则编码" prop="ruleCode" width="180" />
        <el-table-column label="规则名称" prop="ruleName" width="150" />
        <el-table-column label="规则描述" prop="description" min-width="250" show-overflow-tooltip />
        <el-table-column label="参数配置" width="200">
          <template slot-scope="scope">
            <div v-for="(value, key) in scope.row.params" :key="key" class="param-item">
              <span class="param-key">{{ getParamLabel(key) }}:</span>
              <span class="param-value">{{ value }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center">
          <template slot-scope="scope">
            <el-button type="text" size="mini" icon="el-icon-edit" @click="handleEditRule(scope.row)">
              编辑
            </el-button>
            <el-button type="text" size="mini" icon="el-icon-delete" @click="handleRemoveRule(scope.row.ruleCode)" style="color: #f56c6c;">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top: 15px;">
        <el-button type="primary" size="small" icon="el-icon-plus" @click="showAddRuleDialog">添加规则</el-button>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="rulesDialogVisible = false">关 闭</el-button>
      </div>
    </el-dialog>

    <!-- 添加/编辑规则对话框 -->
    <el-dialog
      :visible.sync="ruleFormDialogVisible"
      :title="ruleFormTitle"
      width="550px"
      :close-on-click-modal="false"
      append-to-body>
      <el-form :model="ruleForm" :rules="ruleFormRules" ref="ruleFormRef" label-width="100px">
        <el-form-item label="规则编码" prop="ruleCode">
          <el-input v-model="ruleForm.ruleCode" placeholder="如: DAWN_USAGE_ABNORMAL" :disabled="isEditRule" />
        </el-form-item>
        <el-form-item label="规则名称" prop="ruleName">
          <el-input v-model="ruleForm.ruleName" placeholder="如: 凌晨用水异常" />
        </el-form-item>
        <el-form-item label="规则描述" prop="description">
          <el-input v-model="ruleForm.description" type="textarea" :rows="2" placeholder="请输入规则描述" />
        </el-form-item>
        <el-form-item label="阈值参数">
          <el-input-number v-model="ruleForm.threshold" :min="0" :max="100" :step="1" :precision="2" />
          <span class="form-tip">触发异常的阈值（如占比%或用水量）</span>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="ruleFormDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitRuleForm">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  listAnomalyRecords,
  handleAnomalyRecord,
  getAnomalyRules,
  addAnomalyRule,
  removeAnomalyRule
} from '@/api/water/cluster'

export default {
  name: 'WaterAnomaly',
  data() {
    return {
      loading: false,
      anomalyList: [],
      total: 0,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        userId: undefined,
        statPeriod: undefined,
        ruleCode: undefined,
        status: undefined
      },
      // 统计概览
      overviewStats: {
        total: 0,
        pending: 0,
        handled: 0,
        ignored: 0
      },
      // 处理对话框
      processDialogVisible: false,
      processDialogTitle: '',
      processForm: {
        anomalyId: undefined,
        ruleCode: '',
        ruleName: '',
        anomalyDescription: '',
        actualValue: 0,
        thresholdValue: 0,
        handleRemark: '',
        status: 1
      },
      // 详情对话框
      detailDialogVisible: false,
      detailData: {},
      // 规则配置
      rulesDialogVisible: false,
      anomalyRules: [],
      // 添加/编辑规则
      ruleFormDialogVisible: false,
      ruleFormTitle: '',
      isEditRule: false,
      ruleForm: {
        ruleCode: '',
        ruleName: '',
        description: '',
        threshold: 10
      },
      ruleFormRules: {
        ruleCode: [
          { required: true, message: '请输入规则编码', trigger: 'blur' }
        ],
        ruleName: [
          { required: true, message: '请输入规则名称', trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    this.fetchAnomalyList()
  },
  methods: {
    // 获取异常记录列表
    fetchAnomalyList() {
      this.loading = true
      listAnomalyRecords(this.queryParams).then(response => {
        this.anomalyList = response.rows
        this.total = response.total
        this.calculateOverviewStats()
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    // 计算统计概览
    calculateOverviewStats() {
      // 简单统计（实际应从后端获取）
      const stats = { total: this.total, pending: 0, handled: 0, ignored: 0 }
      this.anomalyList.forEach(item => {
        if (item.status === 0) stats.pending++
        else if (item.status === 1) stats.handled++
        else if (item.status === 2) stats.ignored++
      })
      this.overviewStats = stats
    },
    // 搜索
    handleQuery() {
      this.queryParams.pageNum = 1
      this.fetchAnomalyList()
    },
    // 重置
    resetQuery() {
      this.queryParams = {
        pageNum: 1,
        pageSize: 10,
        userId: undefined,
        statPeriod: undefined,
        ruleCode: undefined,
        status: undefined
      }
      this.handleQuery()
    },
    // 刷新
    handleRefresh() {
      this.fetchAnomalyList()
    },
    // 处理异常
    handleProcess(row, status) {
      this.processDialogTitle = status === 1 ? '处理异常' : '忽略异常'
      this.processForm = {
        anomalyId: row.anomalyId,
        ruleCode: row.ruleCode,
        ruleName: row.ruleName,
        anomalyDescription: row.anomalyDescription,
        actualValue: row.actualValue,
        thresholdValue: row.thresholdValue,
        handleRemark: '',
        status: status
      }
      this.processDialogVisible = true
    },
    // 提交处理
    submitProcess() {
      handleAnomalyRecord(
        this.processForm.anomalyId,
        this.processForm.status,
        this.processForm.handleRemark
      ).then(() => {
        this.$message.success('处理成功')
        this.processDialogVisible = false
        this.fetchAnomalyList()
      })
    },
    // 查看详情
    handleViewDetail(row) {
      this.detailData = row
      this.detailDialogVisible = true
    },
    // 显示规则配置对话框
    showRulesDialog() {
      this.fetchAnomalyRules()
      this.rulesDialogVisible = true
    },
    // 获取异常规则
    fetchAnomalyRules() {
      getAnomalyRules().then(response => {
        this.anomalyRules = response.data || []
      })
    },
    // 显示添加规则对话框
    showAddRuleDialog() {
      this.ruleFormTitle = '添加规则'
      this.isEditRule = false
      this.ruleForm = {
        ruleCode: '',
        ruleName: '',
        description: '',
        threshold: 10
      }
      this.ruleFormDialogVisible = true
    },
    // 编辑规则
    handleEditRule(row) {
      this.ruleFormTitle = '编辑规则'
      this.isEditRule = true
      this.ruleForm = {
        ruleCode: row.ruleCode,
        ruleName: row.ruleName,
        description: row.description,
        threshold: row.params?.threshold || 10
      }
      this.ruleFormDialogVisible = true
    },
    // 提交规则表单
    submitRuleForm() {
      this.$refs.ruleFormRef.validate((valid) => {
        if (valid) {
          const rule = {
            ruleCode: this.ruleForm.ruleCode,
            ruleName: this.ruleForm.ruleName,
            description: this.ruleForm.description,
            params: {
              threshold: this.ruleForm.threshold
            }
          }
          addAnomalyRule(rule).then(() => {
            this.$message.success(this.isEditRule ? '更新成功' : '添加成功')
            this.ruleFormDialogVisible = false
            this.fetchAnomalyRules()
          })
        }
      })
    },
    // 删除规则
    handleRemoveRule(ruleCode) {
      this.$confirm('确认删除该规则吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        removeAnomalyRule(ruleCode).then(() => {
          this.$message.success('删除成功')
          this.fetchAnomalyRules()
        })
      })
    },
    // 格式化数字
    formatNumber(num) {
      if (num === null || num === undefined) return '0'
      return parseFloat(num).toFixed(2)
    },
    // 获取异常类型名称
    getAnomalyTypeName(type) {
      const typeMap = {
        'NIGHT_USAGE_ABNORMAL': '凌晨用水异常',
        'DAWN_USAGE_ABNORMAL': '凌晨用水异常',
        'HIGH_DAILY_USAGE': '日均用水异常',
        'WEEKEND_USAGE_ABNORMAL': '周末用水异常'
      }
      return typeMap[type] || type
    },
    // 获取异常类型标签类型
    getAnomalyTypeTagType(type) {
      const typeMap = {
        'NIGHT_USAGE_ABNORMAL': 'warning',
        'DAWN_USAGE_ABNORMAL': 'warning',
        'HIGH_DAILY_USAGE': 'danger',
        'WEEKEND_USAGE_ABNORMAL': 'info'
      }
      return typeMap[type] || ''
    },
    // 获取状态名称
    getStatusName(status) {
      const statusMap = {
        0: '待处理',
        1: '已处理',
        2: '已忽略'
      }
      return statusMap[status] || '未知'
    },
    // 获取状态标签类型
    getStatusTagType(status) {
      const statusMap = {
        0: 'warning',
        1: 'success',
        2: 'info'
      }
      return statusMap[status] || ''
    },
    // 获取参数标签
    getParamLabel(key) {
      const labelMap = {
        'threshold': '阈值',
        'minValue': '最小值',
        'maxValue': '最大值'
      }
      return labelMap[key] || key
    },
    // 表格行样式
    tableRowClassName({ row }) {
      if (row.status === 0) {
        return 'warning-row'
      }
      return ''
    }
  }
}
</script>

<style scoped>
.app-container {
  padding: 20px;
}

/* 统计概览卡片 */
.overview-card {
  border-radius: 10px;
  overflow: hidden;
}

.overview-total {
  background: linear-gradient(135deg, #F56C6C 0%, #f89898 100%);
}

.overview-pending {
  background: linear-gradient(135deg, #E6A23C 0%, #ebb563 100%);
}

.overview-handled {
  background: linear-gradient(135deg, #67C23A 0%, #85ce61 100%);
}

.overview-ignored {
  background: linear-gradient(135deg, #909399 0%, #c0c4cc 100%);
}

.overview-content {
  display: flex;
  align-items: center;
  padding: 10px;
}

.overview-icon {
  font-size: 36px;
  color: rgba(255, 255, 255, 0.8);
  margin-right: 15px;
}

.overview-info {
  color: #fff;
}

.overview-label {
  font-size: 14px;
  opacity: 0.9;
}

.overview-value {
  font-size: 28px;
  font-weight: bold;
  margin-top: 5px;
}

/* 异常值样式 */
.anomaly-value {
  color: #F56C6C;
  font-weight: bold;
}

/* 参数显示 */
.param-item {
  font-size: 12px;
  line-height: 1.8;
}

.param-key {
  color: #909399;
}

.param-value {
  color: #303133;
  margin-left: 5px;
}

/* 表单提示 */
.form-tip {
  margin-left: 10px;
  color: #909399;
  font-size: 12px;
}

/* 表格样式 */
::v-deep .el-table th {
  background-color: #f5f7fa;
  font-weight: bold;
}

::v-deep .el-table .warning-row {
  background-color: #fdf6ec;
}

/* 对话框样式 */
::v-deep .el-dialog__header {
  background-color: #409eff;
  padding: 15px 20px;
}

::v-deep .el-dialog__title {
  color: #ffffff;
  font-weight: bold;
}

::v-deep .el-dialog__headerbtn .el-dialog__close {
  color: #ffffff;
}

::v-deep .el-dialog__headerbtn .el-dialog__close:hover {
  color: #f0f0f0;
}
</style>

