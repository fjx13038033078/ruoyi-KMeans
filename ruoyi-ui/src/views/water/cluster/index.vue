<template>
  <div class="app-container">
    <!-- 聚类分析页面（业务操作员使用） -->

    <!-- 顶部操作区域 -->
    <el-card class="box-card" shadow="hover" style="margin-bottom: 20px;">
      <div slot="header" class="card-header">
        <span><i class="el-icon-s-data"></i> 用水行为聚类分析</span>
      </div>
      <el-form :inline="true" :model="analysisParams" label-width="100px">
        <el-form-item label="统计周期">
          <el-date-picker
            v-model="analysisParams.statPeriod"
            type="month"
            placeholder="选择月份"
            value-format="yyyy-MM"
            style="width: 150px"
          />
        </el-form-item>
        <el-form-item label="聚类数(K)">
          <el-input-number
            v-model="analysisParams.k"
            :min="2"
            :max="10"
            :step="1"
            style="width: 120px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-s-operation" @click="handleAnalyzeAll" :loading="analyzeLoading">
            一键分析
          </el-button>
          <el-button type="success" icon="el-icon-data-analysis" @click="handleExecuteKMeans" :loading="clusterLoading">
            执行聚类
          </el-button>
          <el-button type="warning" icon="el-icon-warning-outline" @click="handleDetectAnomaly" :loading="anomalyLoading">
            异常检测
          </el-button>
          <el-button type="info" icon="el-icon-setting" @click="showConfigDialog">
            算法配置
          </el-button>
          <el-button icon="el-icon-refresh" @click="handleRefresh">
            刷新数据
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 聚类统计卡片 -->
    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :span="6" v-for="(stat, index) in clusterStats" :key="index">
        <el-card class="stat-card" :class="'stat-card-' + index" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon">
              <i :class="getStatIcon(index)"></i>
            </div>
            <div class="stat-info">
              <div class="stat-label">{{ stat.clusterLabel || ('簇 ' + stat.clusterId) }}</div>
              <div class="stat-value">{{ stat.userCount || 0 }} 人</div>
              <div class="stat-desc">
                日均: {{ formatNumber(stat.avgDailyUsage) }} m³
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6" v-if="clusterStats.length === 0">
        <el-card class="stat-card stat-card-empty" shadow="hover">
          <div class="stat-content">
            <div class="stat-info" style="text-align: center; width: 100%;">
              <div class="stat-label">暂无聚类数据</div>
              <div class="stat-desc">请先执行聚类分析</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 可视化图表区域 -->
    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :span="12">
        <el-card shadow="hover">
          <div slot="header" class="card-header">
            <span><i class="el-icon-pie-chart"></i> 聚类分布</span>
          </div>
          <div ref="clusterPieChart" style="width: 100%; height: 350px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <div slot="header" class="card-header">
            <span><i class="el-icon-s-marketing"></i> 各簇特征对比</span>
          </div>
          <div ref="featureRadarChart" style="width: 100%; height: 350px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 聚类结果列表 -->
    <el-card shadow="hover">
      <div slot="header" class="card-header">
        <span><i class="el-icon-s-grid"></i> 聚类结果明细</span>
        <div style="float: right;">
          <el-select v-model="queryParams.clusterId" placeholder="筛选簇" clearable size="small" style="width: 120px; margin-right: 10px;">
            <el-option v-for="(stat, index) in clusterStats" :key="index" :label="stat.clusterLabel || ('簇 ' + stat.clusterId)" :value="stat.clusterId" />
          </el-select>
          <el-button type="primary" size="small" icon="el-icon-search" @click="fetchClusterResult">查询</el-button>
        </div>
      </div>
      <el-table
        :data="clusterResultList"
        v-loading="tableLoading"
        style="width: 100%"
        border
        stripe
        highlight-current-row>
        <el-table-column label="用户ID" prop="userId" width="100" align="center" />
        <el-table-column label="用户名" prop="userName" width="120" align="center" show-overflow-tooltip />
        <el-table-column label="统计周期" prop="statPeriod" width="100" align="center" />
        <el-table-column label="总用水量(m³)" prop="totalUsage" width="120" align="center">
          <template slot-scope="scope">
            <span>{{ formatNumber(scope.row.totalUsage) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="日均用水量(m³)" prop="avgDailyUsage" width="130" align="center">
          <template slot-scope="scope">
            <el-tag :type="getUsageTagType(scope.row.avgDailyUsage)">
              {{ formatNumber(scope.row.avgDailyUsage) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="用水次数" prop="usageCount" width="90" align="center" />
        <el-table-column label="凌晨占比" prop="dawnRatio" width="90" align="center">
          <template slot-scope="scope">
            <span :class="{ 'high-ratio': scope.row.dawnRatio > 15 }">{{ formatNumber(scope.row.dawnRatio) }}%</span>
          </template>
        </el-table-column>
        <el-table-column label="上午占比" prop="morningRatio" width="90" align="center">
          <template slot-scope="scope">
            {{ formatNumber(scope.row.morningRatio) }}%
          </template>
        </el-table-column>
        <el-table-column label="下午占比" prop="afternoonRatio" width="90" align="center">
          <template slot-scope="scope">
            {{ formatNumber(scope.row.afternoonRatio) }}%
          </template>
        </el-table-column>
        <el-table-column label="晚间占比" prop="eveningRatio" width="90" align="center">
          <template slot-scope="scope">
            {{ formatNumber(scope.row.eveningRatio) }}%
          </template>
        </el-table-column>
        <el-table-column label="周末占比" prop="weekendRatio" width="90" align="center">
          <template slot-scope="scope">
            {{ formatNumber(scope.row.weekendRatio) }}%
          </template>
        </el-table-column>
        <el-table-column label="聚类标签" prop="clusterLabel" width="100" align="center">
          <template slot-scope="scope">
            <el-tag :type="getClusterTagType(scope.row.clusterId)">
              {{ scope.row.clusterLabel || '未分类' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <pagination
        v-show="total > 0"
        :total="total"
        :page.sync="queryParams.pageNum"
        :limit.sync="queryParams.pageSize"
        @pagination="fetchClusterResult"
      />
    </el-card>

    <!-- 算法配置对话框 -->
    <el-dialog
      :visible.sync="configDialogVisible"
      title="K-Means算法配置"
      width="600px"
      :close-on-click-modal="false">
      <el-form :model="clusterConfig" label-width="140px">
        <el-form-item label="聚类数量(K)">
          <el-input-number v-model="clusterConfig.k" :min="2" :max="10" :step="1" />
          <span class="form-tip">建议值：3（节约型、正常型、高耗型）</span>
        </el-form-item>
        <el-form-item label="最大迭代次数">
          <el-input-number v-model="clusterConfig.maxIterations" :min="10" :max="500" :step="10" />
          <span class="form-tip">默认100次</span>
        </el-form-item>
        <el-form-item label="收敛阈值">
          <el-input-number v-model="clusterConfig.convergenceThreshold" :min="0.00001" :max="0.01" :step="0.0001" :precision="5" />
          <span class="form-tip">质心变化小于此值时停止</span>
        </el-form-item>
        <el-form-item label="数据标准化">
          <el-switch v-model="clusterConfig.normalize" active-text="启用" inactive-text="禁用" />
          <span class="form-tip">建议启用Z-score标准化</span>
        </el-form-item>
        <el-form-item label="聚类标签">
          <el-tag v-for="(label, index) in clusterConfig.clusterLabels" :key="index" closable @close="handleRemoveLabel(index)" style="margin-right: 10px;">
            {{ label }}
          </el-tag>
          <el-input
            v-if="labelInputVisible"
            v-model="newLabelInput"
            ref="labelInput"
            size="small"
            style="width: 100px;"
            @keyup.enter.native="handleAddLabel"
            @blur="handleAddLabel"
          />
          <el-button v-else size="small" @click="showLabelInput">+ 添加标签</el-button>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="configDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="handleSaveConfig">保 存</el-button>
      </div>
    </el-dialog>

    <!-- 异常规则配置对话框 -->
    <el-dialog
      :visible.sync="rulesDialogVisible"
      title="异常检测规则配置"
      width="800px"
      :close-on-click-modal="false">
      <el-table :data="anomalyRules" border style="width: 100%">
        <el-table-column label="规则编码" prop="ruleCode" width="150" />
        <el-table-column label="规则名称" prop="ruleName" width="150" />
        <el-table-column label="规则描述" prop="description" min-width="200" show-overflow-tooltip />
        <el-table-column label="参数配置" width="200">
          <template slot-scope="scope">
            <div v-for="(value, key) in scope.row.params" :key="key" class="param-item">
              <span class="param-key">{{ key }}:</span>
              <span class="param-value">{{ value }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center">
          <template slot-scope="scope">
            <el-button type="text" size="mini" icon="el-icon-delete" @click="handleRemoveRule(scope.row.ruleCode)" style="color: #f56c6c;">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div slot="footer" class="dialog-footer">
        <el-button @click="rulesDialogVisible = false">关 闭</el-button>
        <el-button type="primary" @click="fetchAnomalyRules">刷新规则</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  generateFeatureData,
  executeKMeans,
  getClusterConfig,
  updateClusterConfig,
  getClusterResult,
  getClusterStatistics,
  executeAnomalyDetection,
  getAnomalyRules,
  removeAnomalyRule,
  analyzeAll
} from '@/api/water/cluster'
import * as echarts from 'echarts'

export default {
  name: 'WaterCluster',
  data() {
    return {
      // 分析参数
      analysisParams: {
        statPeriod: this.getCurrentMonth(),
        k: 3
      },
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        statPeriod: '',
        clusterId: undefined
      },
      // 加载状态
      analyzeLoading: false,
      clusterLoading: false,
      anomalyLoading: false,
      tableLoading: false,
      // 数据
      clusterStats: [],
      clusterResultList: [],
      total: 0,
      // 配置
      clusterConfig: {
        k: 3,
        maxIterations: 100,
        convergenceThreshold: 0.0001,
        normalize: true,
        clusterLabels: ['节约型', '正常型', '高耗型']
      },
      configDialogVisible: false,
      // 异常规则
      anomalyRules: [],
      rulesDialogVisible: false,
      // 标签输入
      labelInputVisible: false,
      newLabelInput: '',
      // 图表实例
      pieChart: null,
      radarChart: null
    }
  },
  created() {
    this.queryParams.statPeriod = this.analysisParams.statPeriod
    this.fetchClusterConfig()
    this.fetchClusterStatistics()
    this.fetchClusterResult()
  },
  mounted() {
    this.$nextTick(() => {
      this.initCharts()
    })
  },
  methods: {
    // 获取当前月份
    getCurrentMonth() {
      const now = new Date()
      const year = now.getFullYear()
      const month = String(now.getMonth() + 1).padStart(2, '0')
      return `${year}-${month}`
    },
    // 格式化数字
    formatNumber(num) {
      if (num === null || num === undefined) return '0'
      return parseFloat(num).toFixed(2)
    },
    // 初始化图表
    initCharts() {
      this.pieChart = echarts.init(this.$refs.clusterPieChart)
      this.radarChart = echarts.init(this.$refs.featureRadarChart)
      this.renderEmptyCharts()
    },
    // 渲染空图表
    renderEmptyCharts() {
      this.pieChart.setOption({
        title: {
          text: '暂无数据',
          left: 'center',
          top: 'center',
          textStyle: { color: '#999', fontSize: 14 }
        }
      })
      this.radarChart.setOption({
        title: {
          text: '暂无数据',
          left: 'center',
          top: 'center',
          textStyle: { color: '#999', fontSize: 14 }
        }
      })
    },
    // 获取聚类配置
    fetchClusterConfig() {
      getClusterConfig().then(response => {
        if (response.data) {
          this.clusterConfig = response.data
        }
      })
    },
    // 获取聚类统计
    fetchClusterStatistics() {
      getClusterStatistics(this.analysisParams.statPeriod).then(response => {
        if (response.data && response.data.length > 0) {
          // 按聚类ID分组统计
          const statsMap = {}
          response.data.forEach(item => {
            const cid = item.clusterId
            if (cid !== null && cid !== undefined) {
              if (!statsMap[cid]) {
                statsMap[cid] = {
                  clusterId: cid,
                  clusterLabel: item.clusterLabel,
                  userCount: 0,
                  totalUsageSum: 0,
                  avgDailyUsageSum: 0
                }
              }
              statsMap[cid].userCount++
              statsMap[cid].totalUsageSum += parseFloat(item.totalUsage || 0)
              statsMap[cid].avgDailyUsageSum += parseFloat(item.avgDailyUsage || 0)
            }
          })
          // 计算平均值
          this.clusterStats = Object.values(statsMap).map(s => ({
            ...s,
            avgDailyUsage: s.userCount > 0 ? s.avgDailyUsageSum / s.userCount : 0
          }))
          this.renderCharts()
        } else {
          this.clusterStats = []
          this.renderEmptyCharts()
        }
      }).catch(() => {
        this.clusterStats = []
        this.renderEmptyCharts()
      })
    },
    // 获取聚类结果列表
    fetchClusterResult() {
      this.tableLoading = true
      this.queryParams.statPeriod = this.analysisParams.statPeriod
      getClusterResult(this.queryParams).then(response => {
        this.clusterResultList = response.rows
        this.total = response.total
        this.tableLoading = false
      }).catch(() => {
        this.tableLoading = false
      })
    },
    // 渲染图表
    renderCharts() {
      this.renderPieChart()
      this.renderRadarChart()
    },
    // 渲染饼图
    renderPieChart() {
      const data = this.clusterStats.map(s => ({
        name: s.clusterLabel || ('簇 ' + s.clusterId),
        value: s.userCount
      }))
      const colors = ['#67C23A', '#409EFF', '#E6A23C', '#F56C6C', '#909399']
      this.pieChart.setOption({
        title: null,
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c}人 ({d}%)'
        },
        legend: {
          orient: 'vertical',
          left: 'left',
          data: data.map(d => d.name)
        },
        color: colors,
        series: [{
          type: 'pie',
          radius: ['40%', '70%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 10,
            borderColor: '#fff',
            borderWidth: 2
          },
          label: {
            show: true,
            formatter: '{b}\n{c}人'
          },
          emphasis: {
            label: {
              show: true,
              fontSize: 16,
              fontWeight: 'bold'
            }
          },
          data: data
        }]
      })
    },
    // 渲染雷达图
    renderRadarChart() {
      if (this.clusterResultList.length === 0) return

      // 按簇分组计算平均特征
      const clusterFeatures = {}
      this.clusterResultList.forEach(item => {
        const cid = item.clusterId
        if (cid !== null && cid !== undefined) {
          if (!clusterFeatures[cid]) {
            clusterFeatures[cid] = {
              label: item.clusterLabel || ('簇 ' + cid),
              count: 0,
              avgDailyUsage: 0,
              dawnRatio: 0,
              morningRatio: 0,
              afternoonRatio: 0,
              eveningRatio: 0,
              weekendRatio: 0
            }
          }
          clusterFeatures[cid].count++
          clusterFeatures[cid].avgDailyUsage += parseFloat(item.avgDailyUsage || 0)
          clusterFeatures[cid].dawnRatio += parseFloat(item.dawnRatio || 0)
          clusterFeatures[cid].morningRatio += parseFloat(item.morningRatio || 0)
          clusterFeatures[cid].afternoonRatio += parseFloat(item.afternoonRatio || 0)
          clusterFeatures[cid].eveningRatio += parseFloat(item.eveningRatio || 0)
          clusterFeatures[cid].weekendRatio += parseFloat(item.weekendRatio || 0)
        }
      })

      // 计算平均值
      const seriesData = []
      const colors = ['#67C23A', '#409EFF', '#E6A23C', '#F56C6C', '#909399']
      let colorIndex = 0
      Object.values(clusterFeatures).forEach(cf => {
        if (cf.count > 0) {
          seriesData.push({
            name: cf.label,
            value: [
              (cf.avgDailyUsage / cf.count * 100).toFixed(2), // 放大以便显示
              (cf.dawnRatio / cf.count).toFixed(2),
              (cf.morningRatio / cf.count).toFixed(2),
              (cf.afternoonRatio / cf.count).toFixed(2),
              (cf.eveningRatio / cf.count).toFixed(2),
              (cf.weekendRatio / cf.count).toFixed(2)
            ],
            lineStyle: { color: colors[colorIndex % colors.length] },
            areaStyle: { color: colors[colorIndex % colors.length], opacity: 0.2 }
          })
          colorIndex++
        }
      })

      this.radarChart.setOption({
        title: null,
        tooltip: {
          trigger: 'item'
        },
        legend: {
          data: seriesData.map(s => s.name),
          bottom: 0
        },
        radar: {
          indicator: [
            { name: '日均用水', max: 100 },
            { name: '凌晨占比', max: 50 },
            { name: '上午占比', max: 50 },
            { name: '下午占比', max: 50 },
            { name: '晚间占比', max: 50 },
            { name: '周末占比', max: 50 }
          ],
          center: ['50%', '50%'],
          radius: '65%'
        },
        series: [{
          type: 'radar',
          data: seriesData
        }]
      })
    },
    // 一键分析
    handleAnalyzeAll() {
      if (!this.analysisParams.statPeriod) {
        this.$message.warning('请选择统计周期')
        return
      }
      this.analyzeLoading = true
      analyzeAll(this.analysisParams.statPeriod, this.analysisParams.k).then(response => {
        this.$message.success(response.msg || '分析完成')
        this.fetchClusterStatistics()
        this.fetchClusterResult()
        this.analyzeLoading = false
      }).catch(() => {
        this.analyzeLoading = false
      })
    },
    // 执行聚类
    handleExecuteKMeans() {
      if (!this.analysisParams.statPeriod) {
        this.$message.warning('请选择统计周期')
        return
      }
      this.clusterLoading = true
      executeKMeans(this.analysisParams.statPeriod, this.analysisParams.k).then(response => {
        this.$message.success(response.msg || '聚类分析完成')
        this.fetchClusterStatistics()
        this.fetchClusterResult()
        this.clusterLoading = false
      }).catch(() => {
        this.clusterLoading = false
      })
    },
    // 执行异常检测
    handleDetectAnomaly() {
      if (!this.analysisParams.statPeriod) {
        this.$message.warning('请选择统计周期')
        return
      }
      this.anomalyLoading = true
      executeAnomalyDetection(this.analysisParams.statPeriod).then(response => {
        this.$message.success(response.msg || '异常检测完成')
        this.anomalyLoading = false
      }).catch(() => {
        this.anomalyLoading = false
      })
    },
    // 刷新数据
    handleRefresh() {
      this.fetchClusterStatistics()
      this.fetchClusterResult()
    },
    // 显示配置对话框
    showConfigDialog() {
      this.fetchClusterConfig()
      this.configDialogVisible = true
    },
    // 保存配置
    handleSaveConfig() {
      updateClusterConfig(this.clusterConfig).then(() => {
        this.$message.success('配置保存成功')
        this.configDialogVisible = false
      })
    },
    // 移除标签
    handleRemoveLabel(index) {
      this.clusterConfig.clusterLabels.splice(index, 1)
    },
    // 显示标签输入
    showLabelInput() {
      this.labelInputVisible = true
      this.$nextTick(() => {
        this.$refs.labelInput.$refs.input.focus()
      })
    },
    // 添加标签
    handleAddLabel() {
      if (this.newLabelInput) {
        this.clusterConfig.clusterLabels.push(this.newLabelInput)
      }
      this.labelInputVisible = false
      this.newLabelInput = ''
    },
    // 获取异常规则
    fetchAnomalyRules() {
      getAnomalyRules().then(response => {
        this.anomalyRules = response.data || []
      })
    },
    // 显示规则对话框
    showRulesDialog() {
      this.fetchAnomalyRules()
      this.rulesDialogVisible = true
    },
    // 移除规则
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
    // 获取统计图标
    getStatIcon(index) {
      const icons = ['el-icon-s-custom', 'el-icon-s-check', 'el-icon-s-opportunity', 'el-icon-warning']
      return icons[index] || 'el-icon-s-data'
    },
    // 获取用水量标签类型
    getUsageTagType(usage) {
      if (usage >= 0.5) return 'danger'
      if (usage >= 0.2) return 'warning'
      return 'success'
    },
    // 获取聚类标签类型
    getClusterTagType(clusterId) {
      const types = ['success', 'primary', 'warning', 'danger', 'info']
      return types[clusterId % types.length]
    }
  },
  beforeDestroy() {
    if (this.pieChart) {
      this.pieChart.dispose()
    }
    if (this.radarChart) {
      this.radarChart.dispose()
    }
  }
}
</script>

<style scoped>
.app-container {
  padding: 20px;
}

.card-header {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.card-header i {
  margin-right: 8px;
  color: #409eff;
}

/* 统计卡片样式 */
.stat-card {
  border-radius: 10px;
  overflow: hidden;
}

.stat-card-0 {
  background: linear-gradient(135deg, #67C23A 0%, #85ce61 100%);
}

.stat-card-1 {
  background: linear-gradient(135deg, #409EFF 0%, #66b1ff 100%);
}

.stat-card-2 {
  background: linear-gradient(135deg, #E6A23C 0%, #ebb563 100%);
}

.stat-card-3 {
  background: linear-gradient(135deg, #F56C6C 0%, #f89898 100%);
}

.stat-card-empty {
  background: linear-gradient(135deg, #909399 0%, #c0c4cc 100%);
}

.stat-content {
  display: flex;
  align-items: center;
  padding: 10px;
}

.stat-icon {
  font-size: 40px;
  color: rgba(255, 255, 255, 0.8);
  margin-right: 15px;
}

.stat-info {
  color: #fff;
}

.stat-label {
  font-size: 14px;
  opacity: 0.9;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  margin: 5px 0;
}

.stat-desc {
  font-size: 12px;
  opacity: 0.8;
}

/* 表单提示 */
.form-tip {
  margin-left: 10px;
  color: #909399;
  font-size: 12px;
}

/* 高占比警告 */
.high-ratio {
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

/* 表格样式 */
::v-deep .el-table th {
  background-color: #f5f7fa;
  font-weight: bold;
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

::v-deep .el-card__header {
  padding: 12px 20px;
  border-bottom: 1px solid #ebeef5;
  background-color: #fafafa;
}
</style>

