<template>
  <div class="app-container">
    <!-- 我的用水记录页面（普通用户使用） -->
    <div>
      <!-- 顶部统计卡片 -->
      <el-row :gutter="20" style="margin-bottom: 20px;">
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
              <i class="el-icon-document"></i>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.totalCount || 0 }}</div>
              <div class="stat-label">总记录数</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-icon" style="background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);">
              <i class="el-icon-odometer"></i>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.totalUsage || '0.00' }} m³</div>
              <div class="stat-label">总用水量</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
              <i class="el-icon-data-line"></i>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.avgUsage || '0.00' }} m³</div>
              <div class="stat-label">平均用水量</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
              <i class="el-icon-pie-chart"></i>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.mainType || '暂无' }}</div>
              <div class="stat-label">主要用水类型</div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 搜索表单 -->
      <el-form :model="queryParams" ref="queryForm" :inline="true" label-width="80px" style="margin-bottom: 10px;">
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
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
          <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 操作按钮区域 -->
      <el-row :gutter="20" class="mb-20" style="margin-bottom: 10px;">
        <el-col>
          <el-button
            type="primary"
            plain
            icon="el-icon-plus"
            size="medium"
            @click="handleAdd"
          >
            录入用水记录
          </el-button>
          <el-button
            type="success"
            plain
            icon="el-icon-s-data"
            size="medium"
            @click="showMyStatistics"
          >
            我的用水分析
          </el-button>
        </el-col>
      </el-row>

      <!-- 我的用水记录列表 -->
      <el-table
        :data="recordList"
        v-loading="loading"
        style="width: 100%"
        border
        stripe
        highlight-current-row>
        <el-table-column label="ID" prop="recordId" width="80" align="center"></el-table-column>
        <el-table-column label="用水日期" prop="usageDate" width="120" align="center"></el-table-column>
        <el-table-column label="用水量(m³)" prop="usageAmount" width="130" align="center">
          <template slot-scope="scope">
            <el-tag :type="getUsageAmountType(scope.row.usageAmount)" size="medium">
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
        <el-table-column label="用水时段" prop="timePeriod" width="130" align="center">
          <template slot-scope="scope">
            <i :class="getTimePeriodIcon(scope.row.timePeriod)" style="margin-right: 5px;"></i>
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
        <el-table-column label="操作" align="center" width="150" fixed="right">
          <template slot-scope="scope">
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
      @pagination="fetchMyRecords"
    />

    <!-- 添加/编辑记录对话框 -->
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
                style="width: 100%"
                @change="handleDateChange">
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="用水量(m³)" prop="usageAmount">
              <el-input-number
                v-model="form.usageAmount"
                :min="0"
                :max="100"
                :precision="4"
                :step="0.01"
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
              <el-select v-model="form.usageType" placeholder="请选择用水类型" style="width: 100%">
                <el-option label="饮用" :value="1">
                  <span><i class="el-icon-coffee-cup" style="margin-right: 5px;"></i>饮用</span>
                </el-option>
                <el-option label="烹饪" :value="2">
                  <span><i class="el-icon-food" style="margin-right: 5px;"></i>烹饪</span>
                </el-option>
                <el-option label="洗浴" :value="3">
                  <span><i class="el-icon-hot-water" style="margin-right: 5px;"></i>洗浴</span>
                </el-option>
                <el-option label="洗衣" :value="4">
                  <span><i class="el-icon-brush" style="margin-right: 5px;"></i>洗衣</span>
                </el-option>
                <el-option label="冲厕" :value="5">
                  <span><i class="el-icon-toilet-paper" style="margin-right: 5px;"></i>冲厕</span>
                </el-option>
                <el-option label="其他" :value="6">
                  <span><i class="el-icon-more" style="margin-right: 5px;"></i>其他</span>
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="用水时段" prop="timePeriod">
              <el-select v-model="form.timePeriod" placeholder="请选择用水时段" style="width: 100%">
                <el-option label="凌晨(0-6点)" :value="1">
                  <span><i class="el-icon-moon" style="margin-right: 5px;"></i>凌晨(0-6点)</span>
                </el-option>
                <el-option label="上午(6-12点)" :value="2">
                  <span><i class="el-icon-sunrise" style="margin-right: 5px;"></i>上午(6-12点)</span>
                </el-option>
                <el-option label="下午(12-18点)" :value="3">
                  <span><i class="el-icon-sunny" style="margin-right: 5px;"></i>下午(12-18点)</span>
                </el-option>
                <el-option label="晚间(18-24点)" :value="4">
                  <span><i class="el-icon-sunset" style="margin-right: 5px;"></i>晚间(18-24点)</span>
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="是否周末" prop="isWeekend">
          <el-radio-group v-model="form.isWeekend">
            <el-radio :label="0">否（工作日）</el-radio>
            <el-radio :label="1">是（周末）</el-radio>
          </el-radio-group>
          <div style="font-size: 12px; color: #909399; margin-top: 5px;">
            提示：选择日期后会自动判断是否为周末
          </div>
        </el-form-item>

        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注（可选）"
            maxlength="500"
            show-word-limit>
          </el-input>
        </el-form-item>
      </el-form>
      <!-- 对话框按钮 -->
      <div slot="footer" class="dialog-footer" style="text-align: center;">
        <el-button @click="handleCloseDialog">取消</el-button>
        <el-button type="primary" @click="handleSubmit">{{ dialogButtonText }}</el-button>
      </div>
    </el-dialog>

    <!-- 我的用水分析对话框 -->
    <el-dialog
      :visible.sync="analysisDialogVisible"
      title="我的用水分析"
      width="900px"
      :close-on-click-modal="false">
      <el-row :gutter="20">
        <el-col :span="12">
          <div class="chart-container">
            <h4>用水类型分布</h4>
            <div ref="myTypeChart" style="width: 100%; height: 300px;"></div>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="chart-container">
            <h4>用水时段分布</h4>
            <div ref="myPeriodChart" style="width: 100%; height: 300px;"></div>
          </div>
        </el-col>
      </el-row>
      <div slot="footer" class="dialog-footer">
        <el-button @click="analysisDialogVisible = false">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  getMyRecords,
  addRecord,
  updateRecord,
  deleteRecord,
  getMyUsageTypeStatistics,
  getMyTimePeriodStatistics
} from '@/api/water/record'
import * as echarts from 'echarts'

export default {
  name: 'MyWaterRecord',
  data() {
    return {
      loading: false,
      recordList: [],
      dialogVisible: false,
      dialogTitle: '',
      dialogButtonText: '',
      total: 0,
      dateRange: [],
      analysisDialogVisible: false,
      myTypeChart: null,
      myPeriodChart: null,
      // 统计数据
      statistics: {
        totalCount: 0,
        totalUsage: '0.00',
        avgUsage: '0.00',
        mainType: '暂无'
      },
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
        usageType: undefined,
        timePeriod: undefined,
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
    this.fetchMyRecords()
  },
  methods: {
    // 获取我的用水记录列表
    fetchMyRecords() {
      this.loading = true
      getMyRecords(this.queryParams).then(response => {
        this.recordList = response.rows
        this.total = response.total
        this.loading = false
        // 计算统计数据
        this.calculateStatistics()
      }).catch(() => {
        this.loading = false
      })
    },
    // 计算统计数据
    calculateStatistics() {
      const records = this.recordList
      this.statistics.totalCount = this.total

      if (records.length > 0) {
        // 计算总用水量
        const totalUsage = records.reduce((sum, r) => sum + parseFloat(r.usageAmount || 0), 0)
        this.statistics.totalUsage = totalUsage.toFixed(2)

        // 计算平均用水量
        this.statistics.avgUsage = (totalUsage / records.length).toFixed(2)

        // 计算主要用水类型
        const typeCount = {}
        records.forEach(r => {
          const type = r.usageType
          typeCount[type] = (typeCount[type] || 0) + 1
        })
        let maxType = null
        let maxCount = 0
        for (const type in typeCount) {
          if (typeCount[type] > maxCount) {
            maxCount = typeCount[type]
            maxType = type
          }
        }
        this.statistics.mainType = this.getUsageTypeName(parseInt(maxType))
      }
    },
    // 新增记录
    handleAdd() {
      this.dialogTitle = '录入用水记录'
      this.dialogButtonText = '确定'
      this.resetForm()
      // 默认设置为今天
      const today = new Date()
      this.form.usageDate = this.formatDate(today)
      this.form.isWeekend = (today.getDay() === 0 || today.getDay() === 6) ? 1 : 0
      this.dialogVisible = true
    },
    // 编辑记录
    handleEdit(row) {
      this.dialogTitle = '编辑用水记录'
      this.dialogButtonText = '更新'
      this.form = { ...row }
      this.dialogVisible = true
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
          this.fetchMyRecords()
        })
      }).catch(() => {
        this.$message.info('已取消删除')
      })
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
              this.fetchMyRecords()
            })
          } else {
            addRecord(this.form).then(() => {
              this.$message.success('录入成功')
              this.dialogVisible = false
              this.resetForm()
              this.fetchMyRecords()
            })
          }
        } else {
          this.$message.error('请填写必填项')
          return false
        }
      })
    },
    // 显示我的用水分析
    showMyStatistics() {
      this.analysisDialogVisible = true
      this.$nextTick(() => {
        this.loadMyStatistics()
      })
    },
    // 加载我的统计数据
    loadMyStatistics() {
      // 用水类型统计
      getMyUsageTypeStatistics().then(response => {
        this.renderMyTypeChart(response.data)
      })
      // 时段统计
      getMyTimePeriodStatistics().then(response => {
        this.renderMyPeriodChart(response.data)
      })
    },
    // 渲染我的用水类型饼图
    renderMyTypeChart(data) {
      if (this.myTypeChart) {
        this.myTypeChart.dispose()
      }
      this.myTypeChart = echarts.init(this.$refs.myTypeChart)
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
        color: ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272'],
        series: [
          {
            type: 'pie',
            radius: ['40%', '70%'],
            avoidLabelOverlap: false,
            label: {
              show: true,
              formatter: '{b}: {d}%'
            },
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
      this.myTypeChart.setOption(option)
    },
    // 渲染我的时段柱状图
    renderMyPeriodChart(data) {
      if (this.myPeriodChart) {
        this.myPeriodChart.dispose()
      }
      this.myPeriodChart = echarts.init(this.$refs.myPeriodChart)
      const categories = ['凌晨\n(0-6点)', '上午\n(6-12点)', '下午\n(12-18点)', '晚间\n(18-24点)']
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
          data: categories,
          axisLabel: {
            interval: 0
          }
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
              color: function(params) {
                const colors = ['#5470c6', '#91cc75', '#fac858', '#ee6666']
                return colors[params.dataIndex]
              },
              borderRadius: [5, 5, 0, 0]
            },
            label: {
              show: true,
              position: 'top',
              formatter: '{c} m³'
            }
          }
        ]
      }
      this.myPeriodChart.setOption(option)
    },
    // 搜索按钮操作
    handleQuery() {
      this.queryParams.pageNum = 1
      this.fetchMyRecords()
    },
    // 重置按钮操作
    resetQuery() {
      this.dateRange = []
      this.queryParams = {
        pageNum: 1,
        pageSize: 10,
        usageType: undefined,
        timePeriod: undefined,
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
    // 获取时段图标
    getTimePeriodIcon(period) {
      const iconMap = {
        1: 'el-icon-moon',
        2: 'el-icon-sunrise',
        3: 'el-icon-sunny',
        4: 'el-icon-sunset'
      }
      return iconMap[period] || ''
    },
    // 获取用水量标签类型
    getUsageAmountType(amount) {
      if (amount >= 1) return 'danger'
      if (amount >= 0.5) return 'warning'
      return 'success'
    },
    // 格式化日期
    formatDate(date) {
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    },
    // 格式化日期时间
    formatDateTime(datetime) {
      if (!datetime) return ''
      return datetime
    }
  },
  beforeDestroy() {
    if (this.myTypeChart) {
      this.myTypeChart.dispose()
    }
    if (this.myPeriodChart) {
      this.myPeriodChart.dispose()
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

/* 统计卡片样式 */
.stat-card {
  display: flex;
  align-items: center;
  padding: 15px;
}

.stat-card ::v-deep .el-card__body {
  display: flex;
  align-items: center;
  width: 100%;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
}

.stat-icon i {
  font-size: 28px;
  color: #fff;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 5px;
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

