<template>
  <div class="home-container">
    <!-- 顶部标题区域 -->
    <div class="header-section">
      <div class="header-bg">
        <div class="header-content">
          <div class="title-wrapper">
            <div class="main-title">
              <i class="el-icon-s-data title-icon"></i>
              <span>智慧用水行为分析管理系统</span>
            </div>
            <div class="sub-title">Smart Water Usage Behavior Analysis & Management System</div>
          </div>
          <div class="header-decoration">
            <div class="decoration-circle circle-1"></div>
            <div class="decoration-circle circle-2"></div>
            <div class="decoration-circle circle-3"></div>
          </div>
        </div>
      </div>
    </div>

    <!-- 统计概览卡片 -->
    <el-row :gutter="20" class="overview-row">
      <template v-if="userType === 'admin'">
        <!-- 管理员/业务操作员视图 -->
        <el-col :xs="12" :sm="6">
          <div class="stat-card stat-card-1">
            <div class="stat-icon">
              <i class="el-icon-user"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ overview.user_count || 0 }}</div>
              <div class="stat-label">用户数量</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6">
          <div class="stat-card stat-card-2">
            <div class="stat-icon">
              <i class="el-icon-document"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ overview.record_count || 0 }}</div>
              <div class="stat-label">用水记录</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6">
          <div class="stat-card stat-card-3">
            <div class="stat-icon">
              <i class="el-icon-odometer"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ formatNumber(overview.total_usage) }} <small>m³</small></div>
              <div class="stat-label">总用水量</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6">
          <div class="stat-card stat-card-4">
            <div class="stat-icon">
              <i class="el-icon-warning"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ anomalyStats.pending || 0 }}</div>
              <div class="stat-label">待处理异常</div>
            </div>
          </div>
        </el-col>
      </template>
      <template v-else>
        <!-- 普通用户视图 -->
        <el-col :xs="12" :sm="6">
          <div class="stat-card stat-card-1">
            <div class="stat-icon">
              <i class="el-icon-document"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ overview.record_count || 0 }}</div>
              <div class="stat-label">我的记录</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6">
          <div class="stat-card stat-card-2">
            <div class="stat-icon">
              <i class="el-icon-odometer"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ formatNumber(overview.total_usage) }} <small>m³</small></div>
              <div class="stat-label">总用水量</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6">
          <div class="stat-card stat-card-3">
            <div class="stat-icon">
              <i class="el-icon-data-analysis"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ formatNumber(overview.avg_usage) }} <small>m³</small></div>
              <div class="stat-label">平均用水</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6">
          <div class="stat-card stat-card-4">
            <div class="stat-icon">
              <i class="el-icon-bell"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ alertStats.unread || 0 }}</div>
              <div class="stat-label">未读告警</div>
            </div>
          </div>
        </el-col>
      </template>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :xs="24" :sm="12" :lg="8">
        <el-card shadow="hover" class="chart-card">
          <div slot="header" class="card-header">
            <span><i class="el-icon-pie-chart"></i> 用水类型分布</span>
          </div>
          <div ref="typeChart" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="8">
        <el-card shadow="hover" class="chart-card">
          <div slot="header" class="card-header">
            <span><i class="el-icon-s-marketing"></i> 用水时段分布</span>
          </div>
          <div ref="periodChart" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :lg="8">
        <el-card shadow="hover" class="chart-card">
          <div slot="header" class="card-header">
            <span><i class="el-icon-data-line"></i> 近7天用水趋势</span>
          </div>
          <div ref="trendChart" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 底部区域：通知公告 + 站外链接 -->
    <el-row :gutter="20" class="bottom-row">
      <el-col :xs="24" :lg="12">
        <el-card shadow="hover" class="notice-card">
          <div slot="header" class="card-header">
            <span><i class="el-icon-bell"></i> 通知公告</span>
          </div>
          <el-table v-loading="loading" :data="noticeList" :show-header="false" class="notice-table">
            <el-table-column width="60" align="center">
              <template slot-scope="scope">
                <el-tag :type="scope.row.noticeType === '1' ? 'warning' : 'success'" size="mini">
                  {{ scope.row.noticeType === '1' ? '通知' : '公告' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="noticeTitle" :show-overflow-tooltip="true">
              <template slot-scope="scope">
                <span class="notice-title" @click="showNoticeContent(scope.row)">
                  {{ scope.row.noticeTitle }}
                </span>
              </template>
            </el-table-column>
            <el-table-column width="100" align="right">
              <template slot-scope="scope">
                <span class="notice-time">{{ parseTime(scope.row.createTime, '{m}-{d}') }}</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card shadow="hover" class="intro-card">
          <div slot="header" class="card-header">
            <span><i class="el-icon-info"></i> 系统简介</span>
          </div>
          <div class="system-intro">
            <div class="intro-section">
              <div class="intro-icon intro-icon-1">
                <i class="el-icon-s-data"></i>
              </div>
              <div class="intro-content">
                <h4>智能用水分析</h4>
                <p>基于K-means聚类算法，对用户用水行为进行智能分析和用户画像，识别不同用水模式。</p>
              </div>
            </div>
            <div class="intro-section">
              <div class="intro-icon intro-icon-2">
                <i class="el-icon-warning-outline"></i>
              </div>
              <div class="intro-content">
                <h4>异常检测预警</h4>
                <p>自动检测凌晨异常用水、日均用水过高、周末用水异常等情况，及时推送告警通知。</p>
              </div>
            </div>
            <div class="intro-section">
              <div class="intro-icon intro-icon-3">
                <i class="el-icon-user"></i>
              </div>
              <div class="intro-content">
                <h4>多角色管理</h4>
                <p>支持系统管理员、业务操作员、普通用户三种角色，各司其职，高效协作。</p>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 公告内容对话框 -->
    <el-dialog :title="selectedNotice.title" :visible.sync="showNoticeDialog" width="780px" append-to-body>
      <div v-html="selectedNotice.content" class="notice-content"></div>
    </el-dialog>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { parseTime } from '@/utils/ruoyi'
import { getNotice, listNotice } from '@/api/system/notice'
import { getHomeStatistics } from '@/api/water/statistics'

export default {
  name: 'Index',
  dicts: ['sys_notice_status', 'sys_notice_type'],
  data() {
    return {
      loading: true,
      noticeList: [],
      selectedNotice: { title: '', content: '' },
      showNoticeDialog: false,
      // 统计数据
      userType: 'admin',
      overview: {},
      anomalyStats: {},
      alertStats: {},
      typeDistribution: [],
      periodDistribution: [],
      dailyTrend: [],
      // 图表实例
      typeChart: null,
      periodChart: null,
      trendChart: null
    }
  },
  created() {
    this.getNoticeList()
    this.getStatistics()
  },
  mounted() {
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize)
    if (this.typeChart) this.typeChart.dispose()
    if (this.periodChart) this.periodChart.dispose()
    if (this.trendChart) this.trendChart.dispose()
  },
  methods: {
    parseTime,
    // 获取统计数据
    getStatistics() {
      getHomeStatistics().then(response => {
        const data = response.data
        this.userType = data.userType || 'admin'
        this.overview = data.overview || {}
        this.anomalyStats = data.anomalyStats || {}
        this.alertStats = data.alertStats || {}
        this.typeDistribution = data.typeDistribution || []
        this.periodDistribution = data.periodDistribution || []
        this.dailyTrend = data.dailyTrend || []
        // 渲染图表
        this.$nextTick(() => {
          this.initCharts()
        })
      }).catch(() => {
        this.$nextTick(() => {
          this.initCharts()
        })
      })
    },
    // 获取公告列表
    getNoticeList() {
      this.loading = true
      listNotice({ pageNum: 1, pageSize: 5 }).then(response => {
        this.noticeList = response.rows
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    // 显示公告内容
    showNoticeContent(row) {
      getNotice(row.noticeId).then(response => {
        this.selectedNotice.title = response.data.noticeTitle
        this.selectedNotice.content = response.data.noticeContent
        this.showNoticeDialog = true
      })
    },
    // 初始化图表
    initCharts() {
      this.initTypeChart()
      this.initPeriodChart()
      this.initTrendChart()
    },
    // 用水类型分布饼图
    initTypeChart() {
      if (this.typeChart) this.typeChart.dispose()
      this.typeChart = echarts.init(this.$refs.typeChart)
      const typeNames = { 1: '饮用', 2: '烹饪', 3: '洗浴', 4: '洗衣', 5: '冲厕', 6: '其他' }
      const colors = ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272']
      const chartData = this.typeDistribution.map(item => ({
        name: typeNames[item.type] || '其他',
        value: parseFloat(item.total_amount || 0)
      }))
      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c} m³ ({d}%)'
        },
        legend: {
          orient: 'vertical',
          right: 10,
          top: 'center'
        },
        color: colors,
        series: [{
          type: 'pie',
          radius: ['40%', '65%'],
          center: ['35%', '50%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 8,
            borderColor: '#fff',
            borderWidth: 2
          },
          label: { show: false },
          emphasis: {
            label: { show: true, fontSize: 14, fontWeight: 'bold' }
          },
          data: chartData.length > 0 ? chartData : [{ name: '暂无数据', value: 0 }]
        }]
      }
      this.typeChart.setOption(option)
    },
    // 用水时段分布柱状图
    initPeriodChart() {
      if (this.periodChart) this.periodChart.dispose()
      this.periodChart = echarts.init(this.$refs.periodChart)
      const periods = ['凌晨', '上午', '下午', '晚间']
      const values = [0, 0, 0, 0]
      this.periodDistribution.forEach(item => {
        const idx = item.period - 1
        if (idx >= 0 && idx < 4) {
          values[idx] = parseFloat(item.total_amount || 0)
        }
      })
      const colors = ['#6366f1', '#10b981', '#f59e0b', '#8b5cf6']
      const option = {
        tooltip: {
          trigger: 'axis',
          formatter: '{b}: {c} m³'
        },
        grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
        xAxis: {
          type: 'category',
          data: periods,
          axisLine: { lineStyle: { color: '#e0e0e0' } },
          axisLabel: { color: '#666' }
        },
        yAxis: {
          type: 'value',
          name: 'm³',
          axisLine: { show: false },
          axisTick: { show: false },
          splitLine: { lineStyle: { type: 'dashed', color: '#e0e0e0' } }
        },
        series: [{
          type: 'bar',
          barWidth: '50%',
          data: values.map((v, i) => ({
            value: v,
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: colors[i] },
                { offset: 1, color: colors[i] + '80' }
              ]),
              borderRadius: [6, 6, 0, 0]
            }
          }))
        }]
      }
      this.periodChart.setOption(option)
    },
    // 近7天用水趋势折线图
    initTrendChart() {
      if (this.trendChart) this.trendChart.dispose()
      this.trendChart = echarts.init(this.$refs.trendChart)
      const dates = this.dailyTrend.map(item => item.date)
      const values = this.dailyTrend.map(item => parseFloat(item.total_amount || 0))
      const option = {
        tooltip: {
          trigger: 'axis',
          formatter: '{b}: {c} m³'
        },
        grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: dates.length > 0 ? dates : ['暂无'],
          axisLine: { lineStyle: { color: '#e0e0e0' } },
          axisLabel: { color: '#666' }
        },
        yAxis: {
          type: 'value',
          name: 'm³',
          axisLine: { show: false },
          axisTick: { show: false },
          splitLine: { lineStyle: { type: 'dashed', color: '#e0e0e0' } }
        },
        series: [{
          type: 'line',
          smooth: true,
          symbol: 'circle',
          symbolSize: 8,
          data: values.length > 0 ? values : [0],
          lineStyle: { width: 3, color: '#667eea' },
          itemStyle: { color: '#667eea', borderWidth: 2, borderColor: '#fff' },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(102, 126, 234, 0.4)' },
              { offset: 1, color: 'rgba(102, 126, 234, 0.05)' }
            ])
          }
        }]
      }
      this.trendChart.setOption(option)
    },
    // 处理窗口大小变化
    handleResize() {
      if (this.typeChart) this.typeChart.resize()
      if (this.periodChart) this.periodChart.resize()
      if (this.trendChart) this.trendChart.resize()
    },
    // 格式化数字
    formatNumber(num) {
      if (num === null || num === undefined) return '0.00'
      return parseFloat(num).toFixed(2)
    }
  }
}
</script>

<style scoped lang="scss">
.home-container {
  padding: 15px;
  background: #f5f7fa;
  min-height: calc(100vh - 84px);
}

/* 头部区域 */
.header-section {
  margin-bottom: 20px;
}

.header-bg {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 30px 40px;
  position: relative;
  overflow: hidden;
}

.header-content {
  position: relative;
  z-index: 1;
}

.title-wrapper {
  text-align: center;
}

.main-title {
  font-size: 32px;
  font-weight: bold;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 15px;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.2);
}

.title-icon {
  font-size: 40px;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

.sub-title {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
  margin-top: 10px;
  letter-spacing: 2px;
}

.header-decoration {
  position: absolute;
  top: 0;
  right: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.decoration-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
}

.circle-1 {
  width: 150px;
  height: 150px;
  top: -50px;
  right: -30px;
}

.circle-2 {
  width: 100px;
  height: 100px;
  bottom: -30px;
  right: 100px;
}

.circle-3 {
  width: 80px;
  height: 80px;
  top: 20px;
  right: 200px;
}

/* 统计卡片 */
.overview-row {
  margin-bottom: 20px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  margin-bottom: 15px;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.12);
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
}

.stat-icon i {
  font-size: 28px;
  color: #fff;
}

.stat-card-1 .stat-icon { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.stat-card-2 .stat-icon { background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%); }
.stat-card-3 .stat-icon { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
.stat-card-4 .stat-icon { background: linear-gradient(135deg, #fa709a 0%, #fee140 100%); }

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 26px;
  font-weight: bold;
  color: #303133;
  line-height: 1.2;
}

.stat-value small {
  font-size: 14px;
  font-weight: normal;
  color: #909399;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 5px;
}

/* 图表区域 */
.chart-row {
  margin-bottom: 20px;
}

.chart-card {
  border-radius: 12px;
  margin-bottom: 15px;
}

.chart-card ::v-deep .el-card__header {
  padding: 15px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.card-header {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.card-header i {
  margin-right: 8px;
  color: #667eea;
}

.chart-container {
  height: 280px;
}

/* 底部区域 */
.bottom-row {
  margin-bottom: 0;
}

.notice-card, .intro-card {
  border-radius: 12px;
  margin-bottom: 15px;
}

.notice-card ::v-deep .el-card__header,
.intro-card ::v-deep .el-card__header {
  padding: 15px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.notice-table {
  margin-top: -10px;
}

.notice-table ::v-deep .el-table__row {
  cursor: pointer;
}

.notice-table ::v-deep .el-table__row:hover {
  background-color: #f5f7fa !important;
}

.notice-title {
  color: #303133;
  transition: color 0.3s;
}

.notice-title:hover {
  color: #667eea;
}

.notice-time {
  font-size: 12px;
  color: #909399;
}

/* 系统简介样式 */
.system-intro {
  padding: 10px 0;
}

.intro-section {
  display: flex;
  align-items: flex-start;
  padding: 15px 0;
  border-bottom: 1px dashed #ebeef5;
}

.intro-section:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.intro-icon {
  width: 50px;
  height: 50px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  flex-shrink: 0;
}

.intro-icon i {
  font-size: 24px;
  color: #fff;
}

.intro-icon-1 {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.intro-icon-2 {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.intro-icon-3 {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
}

.intro-content {
  flex: 1;
}

.intro-content h4 {
  margin: 0 0 8px 0;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.intro-content p {
  margin: 0;
  font-size: 13px;
  color: #909399;
  line-height: 1.6;
}

/* 公告内容 */
.notice-content ::v-deep img {
  max-width: 100%;
  height: auto;
  display: block;
  margin: 0 auto;
}

/* 对话框样式 */
::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 15px 20px;
  border-radius: 4px 4px 0 0;
}

::v-deep .el-dialog__title {
  color: #fff;
  font-weight: bold;
}

::v-deep .el-dialog__headerbtn .el-dialog__close {
  color: #fff;
}
</style>
