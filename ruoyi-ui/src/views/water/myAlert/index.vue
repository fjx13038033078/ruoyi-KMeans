<template>
  <div class="app-container">
    <!-- 我的告警页面（普通用户使用） -->

    <!-- 顶部统计卡片 -->
    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card stat-total">
          <div class="stat-content">
            <div class="stat-icon">
              <i class="el-icon-bell"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.total }}</div>
              <div class="stat-label">告警总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card stat-unread">
          <div class="stat-content">
            <div class="stat-icon">
              <i class="el-icon-message-solid"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.unread }}</div>
              <div class="stat-label">未读告警</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card stat-pending">
          <div class="stat-content">
            <div class="stat-icon">
              <i class="el-icon-question"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.pendingFeedback }}</div>
              <div class="stat-label">待反馈</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 筛选和操作区域 -->
    <el-card shadow="hover" style="margin-bottom: 20px;">
      <el-row :gutter="20">
        <el-col :span="18">
          <el-radio-group v-model="queryParams.isRead" size="medium" @change="handleQuery">
            <el-radio-button :label="null">全部</el-radio-button>
            <el-radio-button :label="0">
              未读
              <el-badge v-if="statistics.unread > 0" :value="statistics.unread" class="badge-item" />
            </el-radio-button>
            <el-radio-button :label="1">已读</el-radio-button>
          </el-radio-group>
          <el-select
            v-model="queryParams.userFeedback"
            placeholder="反馈状态"
            clearable
            size="medium"
            style="margin-left: 15px; width: 130px;"
            @change="handleQuery"
          >
            <el-option label="未反馈" :value="0" />
            <el-option label="确认属实" :value="1" />
            <el-option label="认为误报" :value="2" />
          </el-select>
        </el-col>
        <el-col :span="6" style="text-align: right;">
          <el-button
            type="primary"
            plain
            icon="el-icon-check"
            size="medium"
            :disabled="statistics.unread === 0"
            @click="handleMarkAllRead"
          >
            全部已读
          </el-button>
          <el-button
            icon="el-icon-refresh"
            size="medium"
            @click="handleRefresh"
          >
            刷新
          </el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 告警列表 -->
    <div class="alert-list">
      <el-card
        v-for="item in alertList"
        :key="item.anomalyId"
        shadow="hover"
        class="alert-card"
        :class="{ 'alert-unread': item.isRead === 0 }"
        @click.native="handleViewDetail(item)"
      >
        <div class="alert-header">
          <div class="alert-title">
            <el-tag
              :type="getSeverityType(item.severity)"
              size="small"
              effect="dark"
              style="margin-right: 10px;"
            >
              {{ getSeverityName(item.severity) }}
            </el-tag>
            <span class="rule-name">{{ item.ruleName }}</span>
            <el-tag v-if="item.isRead === 0" type="danger" size="mini" style="margin-left: 10px;">未读</el-tag>
          </div>
          <div class="alert-time">
            <i class="el-icon-time"></i>
            {{ item.createTime }}
          </div>
        </div>
        <div class="alert-body">
          <div class="alert-desc">{{ item.anomalyDescription }}</div>
          <div class="alert-meta">
            <span class="meta-item">
              <i class="el-icon-date"></i>
              统计周期：{{ item.statPeriod }}
            </span>
            <span class="meta-item">
              <i class="el-icon-data-analysis"></i>
              实际值：{{ item.actualValue }} / 阈值：{{ item.thresholdValue }}
            </span>
          </div>
        </div>
        <div class="alert-footer">
          <div class="feedback-status">
            <el-tag v-if="item.userFeedback === 0" type="info" size="small">待反馈</el-tag>
            <el-tag v-else-if="item.userFeedback === 1" type="warning" size="small">
              <i class="el-icon-check"></i> 已确认属实
            </el-tag>
            <el-tag v-else-if="item.userFeedback === 2" type="success" size="small">
              <i class="el-icon-close"></i> 已报告误报
            </el-tag>
          </div>
          <div class="alert-actions">
            <el-button
              v-if="item.userFeedback === 0"
              type="warning"
              size="mini"
              @click.stop="handleFeedback(item, 1)"
            >
              确认属实
            </el-button>
            <el-button
              v-if="item.userFeedback === 0"
              type="success"
              size="mini"
              @click.stop="handleFeedback(item, 2)"
            >
              认为误报
            </el-button>
            <el-button
              type="primary"
              size="mini"
              plain
              @click.stop="handleViewDetail(item)"
            >
              查看详情
            </el-button>
          </div>
        </div>
      </el-card>

      <!-- 空状态 -->
      <el-empty v-if="alertList.length === 0 && !loading" description="暂无告警信息" />
    </div>

    <!-- 分页 -->
    <pagination
      v-show="total > 0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="fetchAlertList"
    />

    <!-- 告警详情对话框 -->
    <el-dialog
      :visible.sync="detailDialogVisible"
      title="告警详情"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-descriptions :column="2" border v-if="currentAlert">
        <el-descriptions-item label="告警类型" :span="2">
          <el-tag :type="getSeverityType(currentAlert.severity)" effect="dark">
            {{ currentAlert.ruleName }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="严重程度">
          {{ getSeverityName(currentAlert.severity) }}
        </el-descriptions-item>
        <el-descriptions-item label="统计周期">
          {{ currentAlert.statPeriod }}
        </el-descriptions-item>
        <el-descriptions-item label="异常描述" :span="2">
          {{ currentAlert.anomalyDescription }}
        </el-descriptions-item>
        <el-descriptions-item label="实际值">
          <span style="color: #f56c6c; font-weight: bold;">{{ currentAlert.actualValue }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="阈值">
          {{ currentAlert.thresholdValue }}
        </el-descriptions-item>
        <el-descriptions-item label="检测字段">
          {{ currentAlert.featureField }}
        </el-descriptions-item>
        <el-descriptions-item label="检测时间">
          {{ currentAlert.createTime }}
        </el-descriptions-item>
        <el-descriptions-item label="处理状态" :span="2">
          <el-tag v-if="currentAlert.status === 0" type="warning">待处理</el-tag>
          <el-tag v-else-if="currentAlert.status === 1" type="success">已处理</el-tag>
          <el-tag v-else-if="currentAlert.status === 2" type="info">已忽略</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="我的反馈" :span="2">
          <template v-if="currentAlert.userFeedback === 0">
            <el-tag type="info">未反馈</el-tag>
          </template>
          <template v-else-if="currentAlert.userFeedback === 1">
            <el-tag type="warning">确认属实</el-tag>
            <span v-if="currentAlert.feedbackRemark" style="margin-left: 10px; color: #909399;">
              {{ currentAlert.feedbackRemark }}
            </span>
          </template>
          <template v-else-if="currentAlert.userFeedback === 2">
            <el-tag type="success">认为误报</el-tag>
            <span v-if="currentAlert.feedbackRemark" style="margin-left: 10px; color: #909399;">
              {{ currentAlert.feedbackRemark }}
            </span>
          </template>
        </el-descriptions-item>
        <el-descriptions-item v-if="currentAlert.handleBy" label="处理人">
          {{ currentAlert.handleBy }}
        </el-descriptions-item>
        <el-descriptions-item v-if="currentAlert.handleRemark" label="处理意见">
          {{ currentAlert.handleRemark }}
        </el-descriptions-item>
      </el-descriptions>

      <!-- 反馈表单（未反馈时显示） -->
      <div v-if="currentAlert && currentAlert.userFeedback === 0" class="feedback-form">
        <el-divider content-position="left">提交反馈</el-divider>
        <el-form :model="feedbackForm" label-width="80px">
          <el-form-item label="反馈类型">
            <el-radio-group v-model="feedbackForm.userFeedback">
              <el-radio :label="1">
                <el-tag type="warning" size="small">确认属实</el-tag>
                <span style="margin-left: 5px; color: #909399;">我确认存在这个问题</span>
              </el-radio>
              <el-radio :label="2">
                <el-tag type="success" size="small">认为误报</el-tag>
                <span style="margin-left: 5px; color: #909399;">我认为检测有误</span>
              </el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="说明">
            <el-input
              v-model="feedbackForm.feedbackRemark"
              type="textarea"
              :rows="3"
              placeholder="请补充说明（可选）"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
        </el-form>
      </div>

      <div slot="footer" class="dialog-footer">
        <el-button @click="detailDialogVisible = false">关 闭</el-button>
        <el-button
          v-if="currentAlert && currentAlert.userFeedback === 0"
          type="primary"
          :disabled="!feedbackForm.userFeedback"
          @click="handleSubmitFeedback"
        >
          提交反馈
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  getMyAlertList,
  getUnreadCount,
  getAlertDetail,
  markAllAsRead,
  submitFeedback
} from '@/api/water/alert'

export default {
  name: 'MyAlert',
  data() {
    return {
      loading: false,
      alertList: [],
      total: 0,
      statistics: {
        total: 0,
        unread: 0,
        pendingFeedback: 0
      },
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        isRead: null,
        userFeedback: null
      },
      detailDialogVisible: false,
      currentAlert: null,
      feedbackForm: {
        userFeedback: null,
        feedbackRemark: ''
      }
    }
  },
  created() {
    this.fetchAlertList()
    this.fetchUnreadCount()
  },
  methods: {
    // 获取告警列表
    fetchAlertList() {
      this.loading = true
      getMyAlertList(this.queryParams).then(response => {
        this.alertList = response.rows
        this.total = response.total
        this.loading = false
        // 计算统计数据
        this.calculateStatistics()
      }).catch(() => {
        this.loading = false
      })
    },
    // 获取未读数量
    fetchUnreadCount() {
      getUnreadCount().then(response => {
        this.statistics.unread = response.data || 0
      })
    },
    // 计算统计数据
    calculateStatistics() {
      this.statistics.total = this.total
      // 从当前页面数据中统计（实际可能需要后端返回）
      this.statistics.pendingFeedback = this.alertList.filter(item => item.userFeedback === 0).length
    },
    // 搜索
    handleQuery() {
      this.queryParams.pageNum = 1
      this.fetchAlertList()
    },
    // 刷新
    handleRefresh() {
      this.fetchAlertList()
      this.fetchUnreadCount()
    },
    // 全部标记已读
    handleMarkAllRead() {
      this.$confirm('确认将所有未读告警标记为已读吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }).then(() => {
        markAllAsRead().then(response => {
          this.$message.success(response.msg || '操作成功')
          this.fetchAlertList()
          this.fetchUnreadCount()
        })
      }).catch(() => {})
    },
    // 查看详情
    handleViewDetail(item) {
      getAlertDetail(item.anomalyId).then(response => {
        this.currentAlert = response.data
        this.feedbackForm = {
          userFeedback: null,
          feedbackRemark: ''
        }
        this.detailDialogVisible = true
        // 刷新未读数量
        this.fetchUnreadCount()
        // 更新列表中的已读状态
        item.isRead = 1
      })
    },
    // 快捷反馈
    handleFeedback(item, feedbackType) {
      const typeText = feedbackType === 1 ? '确认属实' : '认为误报'
      this.$confirm(`确认将此告警标记为"${typeText}"吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }).then(() => {
        submitFeedback({
          anomalyId: item.anomalyId,
          userFeedback: feedbackType,
          feedbackRemark: ''
        }).then(() => {
          this.$message.success('反馈提交成功')
          this.fetchAlertList()
          this.fetchUnreadCount()
        })
      }).catch(() => {})
    },
    // 提交反馈（详情对话框中）
    handleSubmitFeedback() {
      if (!this.feedbackForm.userFeedback) {
        this.$message.warning('请选择反馈类型')
        return
      }
      submitFeedback({
        anomalyId: this.currentAlert.anomalyId,
        userFeedback: this.feedbackForm.userFeedback,
        feedbackRemark: this.feedbackForm.feedbackRemark
      }).then(() => {
        this.$message.success('反馈提交成功')
        this.detailDialogVisible = false
        this.fetchAlertList()
        this.fetchUnreadCount()
      })
    },
    // 获取严重程度名称
    getSeverityName(severity) {
      const map = {
        1: '轻微',
        2: '一般',
        3: '较重',
        4: '严重',
        5: '紧急'
      }
      return map[severity] || '未知'
    },
    // 获取严重程度标签类型
    getSeverityType(severity) {
      const map = {
        1: 'info',
        2: '',
        3: 'warning',
        4: 'danger',
        5: 'danger'
      }
      return map[severity] || 'info'
    }
  }
}
</script>

<style scoped>
.app-container {
  padding: 20px;
}

/* 统计卡片 */
.stat-card {
  cursor: default;
}

.stat-card .stat-content {
  display: flex;
  align-items: center;
}

.stat-card .stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
}

.stat-card .stat-icon i {
  font-size: 28px;
  color: #fff;
}

.stat-card .stat-info {
  flex: 1;
}

.stat-card .stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.stat-card .stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 5px;
}

.stat-total .stat-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-unread .stat-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-pending .stat-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

/* 告警列表 */
.alert-list {
  min-height: 300px;
}

.alert-card {
  margin-bottom: 15px;
  cursor: pointer;
  transition: all 0.3s;
  border-left: 4px solid transparent;
}

.alert-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.alert-card.alert-unread {
  border-left-color: #f56c6c;
  background-color: #fef0f0;
}

.alert-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.alert-title {
  display: flex;
  align-items: center;
}

.rule-name {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.alert-time {
  font-size: 13px;
  color: #909399;
}

.alert-body {
  margin-bottom: 12px;
}

.alert-desc {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
  line-height: 1.6;
}

.alert-meta {
  display: flex;
  gap: 20px;
}

.meta-item {
  font-size: 13px;
  color: #909399;
}

.meta-item i {
  margin-right: 4px;
}

.alert-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
}

.alert-actions {
  display: flex;
  gap: 8px;
}

/* 反馈表单 */
.feedback-form {
  margin-top: 20px;
}

.feedback-form .el-radio {
  display: block;
  margin-bottom: 15px;
}

/* 徽章样式 */
.badge-item {
  margin-left: 5px;
}

::v-deep .badge-item .el-badge__content {
  transform: translateY(-2px);
}

/* 对话框样式 */
::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 15px 20px;
}

::v-deep .el-dialog__title {
  color: #fff;
  font-weight: bold;
}

::v-deep .el-dialog__headerbtn .el-dialog__close {
  color: #fff;
}
</style>

