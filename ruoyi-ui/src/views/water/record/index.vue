<template>
  <div class="app-container">
    <!-- 用水记录管理页面（业务操作员使用） -->
    <div>
      <!-- 搜索表单 -->
      <el-form :model="queryParams" ref="queryForm" :inline="true" label-width="80px" style="margin-bottom: 10px;">
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
        <el-form-item label="用水日期" prop="dateRange">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="yyyy-MM-dd"
            size="small"
            style="width: 240px"
          />
        </el-form-item>
        <el-form-item label="用水类型" prop="usageType">
          <el-select v-model="queryParams.usageType" placeholder="选择类型" clearable size="small" style="width: 120px">
            <el-option label="饮用" :value="1"></el-option>
            <el-option label="烹饪" :value="2"></el-option>
            <el-option label="洗浴" :value="3"></el-option>
            <el-option label="洗衣" :value="4"></el-option>
            <el-option label="冲厕" :value="5"></el-option>
            <el-option label="其他" :value="6"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="用水时段" prop="timePeriod">
          <el-select v-model="queryParams.timePeriod" placeholder="选择时段" clearable size="small" style="width: 140px">
            <el-option label="凌晨(0-6点)" :value="1"></el-option>
            <el-option label="上午(6-12点)" :value="2"></el-option>
            <el-option label="下午(12-18点)" :value="3"></el-option>
            <el-option label="晚间(18-24点)" :value="4"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="是否周末" prop="isWeekend">
          <el-select v-model="queryParams.isWeekend" placeholder="选择" clearable size="small" style="width: 100px">
            <el-option label="否" :value="0"></el-option>
            <el-option label="是" :value="1"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
          <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 操作按钮区域 -->
      <el-row :gutter="20" class="mb-20" style="margin-bottom: 10px;">
        <el-col>
          <el-button
            type="danger"
            plain
            icon="el-icon-delete"
            size="medium"
            :disabled="multiple"
            @click="handleDeleteBatch"
          >
            批量删除
          </el-button>
          <el-button
            type="success"
            plain
            icon="el-icon-s-data"
            size="medium"
            @click="showStatistics"
          >
            统计分析
          </el-button>
        </el-col>
      </el-row>

      <!-- 用水记录列表 -->
      <el-table
        :data="recordList"
        v-loading="loading"
        style="width: 100%"
        border
        stripe
        highlight-current-row
        @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="ID" prop="recordId" width="80" align="center"></el-table-column>
        <el-table-column label="用户名" prop="userName" width="120" align="center" show-overflow-tooltip></el-table-column>
        <el-table-column label="用水日期" prop="usageDate" width="120" align="center"></el-table-column>
        <el-table-column label="用水量(m³)" prop="usageAmount" width="120" align="center">
          <template slot-scope="scope">
            <el-tag :type="getUsageAmountType(scope.row.usageAmount)">
              {{ scope.row.usageAmount }} m³
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="用水类型" prop="usageType" width="100" align="center">
          <template slot-scope="scope">
            <el-tag :type="getUsageTypeColor(scope.row.usageType)">
              {{ getUsageTypeName(scope.row.usageType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="用水时段" prop="timePeriod" width="120" align="center">
          <template slot-scope="scope">
            {{ getTimePeriodName(scope.row.timePeriod) }}
          </template>
        </el-table-column>
        <el-table-column label="是否周末" prop="isWeekend" width="80" align="center">
          <template slot-scope="scope">
            <el-tag :type="scope.row.isWeekend === 1 ? 'success' : 'info'" size="small">
              {{ scope.row.isWeekend === 1 ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="160" align="center">
          <template slot-scope="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="备注" prop="remark" min-width="150" align="center" show-overflow-tooltip></el-table-column>
        <el-table-column label="操作" align="center" width="180" fixed="right">
          <template slot-scope="scope">
            <el-button
              type="text"
              icon="el-icon-view"
              size="mini"
              @click="handleView(scope.row)"
            >
              查看
            </el-button>
            <el-button
              type="text"
              icon="el-icon-edit"
              size="mini"
              @click="handleEdit(scope.row)"
            >
              编辑
            </el-button>
            <el-button
              type="text"
              icon="el-icon-delete"
              size="mini"
              @click="handleDelete(scope.row)"
              style="color: #f56c6c;"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页组件 -->
    <pagination
      v-show="total > 0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="fetchRecordList"
    />

    <!-- 添加/编辑/查看记录对话框 -->
    <el-dialog
      :visible.sync="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleCloseDialog"
      :close-on-click-modal="false">
      <el-form :model="form" :rules="formRules" ref="recordForm" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用水日期" prop="usageDate">
              <el-date-picker
                v-model="form.usageDate"
                type="date"
                placeholder="选择日期"
                value-format="yyyy-MM-dd"
                :disabled="isReadOnly"
                style="width: 100%"
                @change="handleDateChange">
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="用水量(m³)" prop="usageAmount">
              <el-input-number
                v-model="form.usageAmount"
                :disabled="isReadOnly"
                :min="0"
                :max="1000"
                :precision="4"
                :step="0.1"
                controls-position="right"
                style="width: 100%"
                placeholder="请输入用水量">
              </el-input-number>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用水类型" prop="usageType">
              <el-select v-model="form.usageType" :disabled="isReadOnly" placeholder="请选择用水类型" style="width: 100%">
                <el-option label="饮用" :value="1"></el-option>
                <el-option label="烹饪" :value="2"></el-option>
                <el-option label="洗浴" :value="3"></el-option>
                <el-option label="洗衣" :value="4"></el-option>
                <el-option label="冲厕" :value="5"></el-option>
                <el-option label="其他" :value="6"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="用水时段" prop="timePeriod">
              <el-select v-model="form.timePeriod" :disabled="isReadOnly" placeholder="请选择用水时段" style="width: 100%">
                <el-option label="凌晨(0-6点)" :value="1"></el-option>
                <el-option label="上午(6-12点)" :value="2"></el-option>
                <el-option label="下午(12-18点)" :value="3"></el-option>
                <el-option label="晚间(18-24点)" :value="4"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="是否周末" prop="isWeekend">
          <el-radio-group v-model="form.isWeekend" :disabled="isReadOnly">
            <el-radio :label="0">否</el-radio>
            <el-radio :label="1">是</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="form.remark"
            :disabled="isReadOnly"
            type="textarea"
            :rows="3"
            placeholder="请输入备注"
            maxlength="500"
            show-word-limit>
          </el-input>
        </el-form-item>
      </el-form>
      <!-- 对话框按钮 -->
      <div slot="footer" class="dialog-footer" style="text-align: center;">
        <el-button @click="handleCloseDialog">取消</el-button>
        <el-button type="primary" @click="handleSubmit" v-if="!isReadOnly">{{ dialogButtonText }}</el-button>
      </div>
    </el-dialog>

    <!-- 统计分析对话框 -->
    <el-dialog
      :visible.sync="statisticsDialogVisible"
      title="用水统计分析"
      width="800px"
      :close-on-click-modal="false">
      <el-row :gutter="20">
        <el-col :span="12">
          <div class="chart-container">
            <h4>用水类型分布</h4>
            <div ref="typeChart" style="width: 100%; height: 300px;"></div>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="chart-container">
            <h4>用水时段分布</h4>
            <div ref="periodChart" style="width: 100%; height: 300px;"></div>
          </div>
        </el-col>
      </el-row>
      <div slot="footer" class="dialog-footer">
        <el-button @click="statisticsDialogVisible = false">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  listRecord,
  getRecordById,
  addRecord,
  updateRecord,
  deleteRecord,
  deleteRecordBatch,
  getUsageTypeStatistics,
  getTimePeriodStatistics
} from '@/api/water/record'
import * as echarts from 'echarts'

export default {
  name: 'WaterRecord',
  data() {
    return {
      loading: false,
      recordList: [],
      dialogVisible: false,
      dialogTitle: '',
      dialogButtonText: '',
      total: 0,
      isReadOnly: false,
      multiple: true,
      ids: [],
      dateRange: [],
      statisticsDialogVisible: false,
      typeChart: null,
      periodChart: null,
      // 表单数据
      form: {
        recordId: undefined,
        usageDate: '',
        usageAmount: 0,
        usageType: undefined,
        timePeriod: undefined,
        isWeekend: 0,
        remark: ''
      },
      // 表单验证规则
      formRules: {
        usageDate: [
          { required: true, message: '请选择用水日期', trigger: 'change' }
        ],
        usageAmount: [
          { required: true, message: '请输入用水量', trigger: 'blur' }
        ],
        usageType: [
          { required: true, message: '请选择用水类型', trigger: 'change' }
        ],
        timePeriod: [
          { required: true, message: '请选择用水时段', trigger: 'change' }
        ]
      },
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        userId: undefined,
        usageType: undefined,
        timePeriod: undefined,
        isWeekend: undefined,
        params: {
          beginDate: undefined,
          endDate: undefined
        }
      }
    }
  },
  watch: {
    dateRange(val) {
      if (val && val.length === 2) {
        this.queryParams.params.beginDate = val[0]
        this.queryParams.params.endDate = val[1]
      } else {
        this.queryParams.params.beginDate = undefined
        this.queryParams.params.endDate = undefined
      }
    }
  },
  created() {
    this.fetchRecordList()
  },
  methods: {
    // 获取用水记录列表
    fetchRecordList() {
      this.loading = true
      listRecord(this.queryParams).then(response => {
        this.recordList = response.rows
        this.total = response.total
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    // 查看详情
    handleView(row) {
      this.dialogTitle = '查看用水记录'
      this.dialogButtonText = '关闭'
      this.isReadOnly = true
      getRecordById(row.recordId).then(response => {
        this.form = response.data
        this.dialogVisible = true
      })
    },
    // 新增记录
    handleAdd() {
      this.isReadOnly = false
      this.dialogTitle = '新增用水记录'
      this.dialogButtonText = '确定'
      this.resetForm()
      this.dialogVisible = true
    },
    // 编辑记录
    handleEdit(row) {
      this.isReadOnly = false
      this.dialogTitle = '编辑用水记录'
      this.dialogButtonText = '更新'
      getRecordById(row.recordId).then(response => {
        this.form = response.data
        this.dialogVisible = true
      })
    },
    // 删除记录
    handleDelete(row) {
      this.$confirm('确认删除该用水记录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteRecord(row.recordId).then(() => {
          this.$message.success('删除成功')
          this.fetchRecordList()
        })
      }).catch(() => {
        this.$message.info('已取消删除')
      })
    },
    // 批量删除
    handleDeleteBatch() {
      if (this.ids.length === 0) {
        this.$message.warning('请选择要删除的数据')
        return
      }
      this.$confirm('确认删除选中的用水记录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteRecordBatch(this.ids).then(() => {
          this.$message.success('批量删除成功')
          this.fetchRecordList()
        })
      }).catch(() => {
        this.$message.info('已取消删除')
      })
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.recordId)
      this.multiple = !selection.length
    },
    // 日期变化时自动判断是否为周末
    handleDateChange(val) {
      if (val) {
        const date = new Date(val)
        const dayOfWeek = date.getDay()
        this.form.isWeekend = (dayOfWeek === 0 || dayOfWeek === 6) ? 1 : 0
      }
    },
    // 关闭对话框
    handleCloseDialog() {
      this.dialogVisible = false
      this.resetForm()
    },
    // 重置表单
    resetForm() {
      this.form = {
        recordId: undefined,
        usageDate: '',
        usageAmount: 0,
        usageType: undefined,
        timePeriod: undefined,
        isWeekend: 0,
        remark: ''
      }
      if (this.$refs.recordForm) {
        this.$refs.recordForm.resetFields()
      }
    },
    // 提交表单
    handleSubmit() {
      this.$refs.recordForm.validate((valid) => {
        if (valid) {
          if (this.form.recordId) {
            updateRecord(this.form).then(() => {
              this.$message.success('更新成功')
              this.dialogVisible = false
              this.resetForm()
              this.fetchRecordList()
            })
          } else {
            addRecord(this.form).then(() => {
              this.$message.success('新增成功')
              this.dialogVisible = false
              this.resetForm()
              this.fetchRecordList()
            })
          }
        } else {
          this.$message.error('请填写必填项')
          return false
        }
      })
    },
    // 显示统计分析
    showStatistics() {
      this.statisticsDialogVisible = true
      this.$nextTick(() => {
        this.loadStatistics()
      })
    },
    // 加载统计数据
    loadStatistics() {
      // 用水类型统计
      getUsageTypeStatistics(null).then(response => {
        this.renderTypeChart(response.data)
      })
      // 时段统计
      getTimePeriodStatistics(null).then(response => {
        this.renderPeriodChart(response.data)
      })
    },
    // 渲染用水类型饼图
    renderTypeChart(data) {
      if (this.typeChart) {
        this.typeChart.dispose()
      }
      this.typeChart = echarts.init(this.$refs.typeChart)
      const chartData = data.map(item => ({
        name: this.getUsageTypeName(item.type),
        value: parseFloat(item.total_amount || 0)
      }))
      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c} m³ ({d}%)'
        },
        legend: {
          orient: 'vertical',
          left: 'left'
        },
        series: [
          {
            type: 'pie',
            radius: '60%',
            data: chartData,
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.5)'
              }
            }
          }
        ]
      }
      this.typeChart.setOption(option)
    },
    // 渲染时段柱状图
    renderPeriodChart(data) {
      if (this.periodChart) {
        this.periodChart.dispose()
      }
      this.periodChart = echarts.init(this.$refs.periodChart)
      const categories = ['凌晨', '上午', '下午', '晚间']
      const values = [0, 0, 0, 0]
      data.forEach(item => {
        const idx = item.period - 1
        if (idx >= 0 && idx < 4) {
          values[idx] = parseFloat(item.total_amount || 0)
        }
      })
      const option = {
        tooltip: {
          trigger: 'axis',
          formatter: '{b}: {c} m³'
        },
        xAxis: {
          type: 'category',
          data: categories
        },
        yAxis: {
          type: 'value',
          name: '用水量(m³)'
        },
        series: [
          {
            type: 'bar',
            data: values,
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: '#83bff6' },
                { offset: 0.5, color: '#188df0' },
                { offset: 1, color: '#188df0' }
              ])
            }
          }
        ]
      }
      this.periodChart.setOption(option)
    },
    // 搜索按钮操作
    handleQuery() {
      this.queryParams.pageNum = 1
      this.fetchRecordList()
    },
    // 重置按钮操作
    resetQuery() {
      this.dateRange = []
      this.queryParams = {
        pageNum: 1,
        pageSize: 10,
        userId: undefined,
        usageType: undefined,
        timePeriod: undefined,
        isWeekend: undefined,
        params: {
          beginDate: undefined,
          endDate: undefined
        }
      }
      this.handleQuery()
    },
    // 获取用水类型名称
    getUsageTypeName(type) {
      const typeMap = {
        1: '饮用',
        2: '烹饪',
        3: '洗浴',
        4: '洗衣',
        5: '冲厕',
        6: '其他'
      }
      return typeMap[type] || '未知'
    },
    // 获取用水类型颜色
    getUsageTypeColor(type) {
      const colorMap = {
        1: 'primary',
        2: 'success',
        3: 'warning',
        4: 'info',
        5: 'danger',
        6: ''
      }
      return colorMap[type] || ''
    },
    // 获取时段名称
    getTimePeriodName(period) {
      const periodMap = {
        1: '凌晨(0-6点)',
        2: '上午(6-12点)',
        3: '下午(12-18点)',
        4: '晚间(18-24点)'
      }
      return periodMap[period] || '未知'
    },
    // 获取用水量标签类型
    getUsageAmountType(amount) {
      if (amount >= 1) return 'danger'
      if (amount >= 0.5) return 'warning'
      return 'success'
    },
    // 格式化日期时间
    formatDateTime(datetime) {
      if (!datetime) return ''
      return datetime
    }
  },
  beforeDestroy() {
    if (this.typeChart) {
      this.typeChart.dispose()
    }
    if (this.periodChart) {
      this.periodChart.dispose()
    }
  }
}
</script>

<style scoped>
.app-container {
  padding: 20px;
}

.mb-20 {
  margin-bottom: 20px;
}

.chart-container {
  background: #fff;
  padding: 15px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.chart-container h4 {
  text-align: center;
  margin-bottom: 10px;
  color: #303133;
}

/* 表格样式优化 */
::v-deep .el-table th {
  background-color: #f5f7fa;
  font-weight: bold;
}

::v-deep .el-table--striped .el-table__body tr.el-table__row--striped td {
  background: #fafafa;
}

/* 对话框样式优化 */
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

