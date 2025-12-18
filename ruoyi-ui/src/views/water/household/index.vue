<template>
  <div class="app-container">
    <!-- 住户信息管理页面 -->
    <div>
      <!-- 搜索表单 -->
      <el-form :model="queryParams" ref="queryForm" :inline="true" label-width="80px" style="margin-bottom: 10px;">
        <el-form-item label="所属小区" prop="community">
          <el-input
            v-model="queryParams.community"
            placeholder="请输入所属小区"
            clearable
            size="small"
            style="width: 200px"
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>
        <el-form-item label="住址" prop="address">
          <el-input
            v-model="queryParams.address"
            placeholder="请输入住址"
            clearable
            size="small"
            style="width: 200px"
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>
        <el-form-item label="水表编号" prop="meterNo">
          <el-input
            v-model="queryParams.meterNo"
            placeholder="请输入水表编号"
            clearable
            size="small"
            style="width: 150px"
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="选择状态" clearable size="small" style="width: 120px">
            <el-option label="正常" :value="0"></el-option>
            <el-option label="停用" :value="1"></el-option>
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
            v-hasPermi="['water:household:add']"
          >
            新增住户
          </el-button>
          <el-button
            type="danger"
            plain
            icon="el-icon-delete"
            size="medium"
            :disabled="multiple"
            @click="handleDeleteBatch"
            v-hasPermi="['water:household:delete']"
          >
            批量删除
          </el-button>
        </el-col>
      </el-row>

      <!-- 住户信息列表 -->
      <el-table
        :data="householdList"
        v-loading="loading"
        style="width: 100%"
        border
        stripe
        highlight-current-row
        @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="ID" prop="householdId" width="80" align="center"></el-table-column>
        <el-table-column label="用户名" prop="userName" width="120" align="center" show-overflow-tooltip></el-table-column>
        <el-table-column label="所属小区" prop="community" min-width="150" align="center" show-overflow-tooltip></el-table-column>
        <el-table-column label="住址" prop="address" min-width="200" align="center" show-overflow-tooltip></el-table-column>
        <el-table-column label="家庭人数" prop="familySize" width="100" align="center">
          <template slot-scope="scope">
            <el-tag type="info">{{ scope.row.familySize }} 人</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="水表编号" prop="meterNo" width="150" align="center" show-overflow-tooltip></el-table-column>
        <el-table-column label="状态" prop="status" width="80" align="center">
          <template slot-scope="scope">
            <el-switch
              v-model="scope.row.status"
              :active-value="0"
              :inactive-value="1"
              @change="handleStatusChange(scope.row)"
            ></el-switch>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="160" align="center">
          <template slot-scope="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="备注" prop="remark" min-width="150" align="center" show-overflow-tooltip></el-table-column>
        <el-table-column label="操作" align="center" width="200" fixed="right">
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
              v-hasPermi="['water:household:edit']"
            >
              编辑
            </el-button>
            <el-button
              type="text"
              icon="el-icon-delete"
              size="mini"
              @click="handleDelete(scope.row)"
              v-hasPermi="['water:household:delete']"
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
      @pagination="fetchHouseholdList"
    />

    <!-- 添加/编辑/查看住户对话框 -->
    <el-dialog
      :visible.sync="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleCloseDialog"
      :close-on-click-modal="false">
      <el-form :model="form" :rules="formRules" ref="householdForm" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="关联用户" prop="userId">
              <el-select
                v-model="form.userId"
                filterable
                remote
                reserve-keyword
                placeholder="请输入用户名搜索"
                :remote-method="searchUsers"
                :loading="userLoading"
                :disabled="isReadOnly"
                style="width: 100%">
                <el-option
                  v-for="user in userList"
                  :key="user.userId"
                  :label="user.nickName"
                  :value="user.userId">
                  <span>{{ user.nickName }}</span>
                  <span style="float: right; color: #8492a6; font-size: 13px">{{ user.userName }}</span>
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="家庭人数" prop="familySize">
              <el-input-number
                v-model="form.familySize"
                :disabled="isReadOnly"
                :min="1"
                :max="20"
                controls-position="right"
                style="width: 100%"
                placeholder="请输入家庭人数">
              </el-input-number>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="所属小区" prop="community">
          <el-input v-model="form.community" :disabled="isReadOnly" placeholder="请输入所属小区"></el-input>
        </el-form-item>

        <el-form-item label="住址" prop="address">
          <el-input v-model="form.address" :disabled="isReadOnly" placeholder="请输入详细住址"></el-input>
        </el-form-item>

        <el-form-item label="水表编号" prop="meterNo">
          <el-input v-model="form.meterNo" :disabled="isReadOnly" placeholder="请输入水表编号"></el-input>
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status" :disabled="isReadOnly">
            <el-radio :label="0">正常</el-radio>
            <el-radio :label="1">停用</el-radio>
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
  </div>
</template>

<script>
import {
  listHousehold,
  getHouseholdById,
  addHousehold,
  updateHousehold,
  deleteHousehold,
  deleteHouseholdBatch,
  updateHouseholdStatus
} from '@/api/water/household'
import { listUser } from '@/api/system/user'

export default {
  name: 'WaterHousehold',
  data() {
    return {
      loading: false,
      householdList: [],
      dialogVisible: false,
      dialogTitle: '',
      dialogButtonText: '',
      total: 0,
      isReadOnly: false,
      multiple: true,
      ids: [],
      // 用户列表
      userList: [],
      userLoading: false,
      // 表单数据
      form: {
        householdId: undefined,
        userId: undefined,
        address: '',
        community: '',
        familySize: 1,
        meterNo: '',
        status: 0,
        remark: ''
      },
      // 表单验证规则
      formRules: {
        userId: [
          { required: true, message: '请选择关联用户', trigger: 'change' }
        ],
        community: [
          { required: true, message: '请输入所属小区', trigger: 'blur' }
        ],
        address: [
          { required: true, message: '请输入住址', trigger: 'blur' }
        ],
        familySize: [
          { required: true, message: '请输入家庭人数', trigger: 'blur' }
        ]
      },
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        community: undefined,
        address: undefined,
        meterNo: undefined,
        status: undefined
      }
    }
  },
  created() {
    this.fetchHouseholdList()
  },
  methods: {
    // 获取住户信息列表
    fetchHouseholdList() {
      this.loading = true
      listHousehold(this.queryParams).then(response => {
        this.householdList = response.rows
        this.total = response.total
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    // 搜索用户
    searchUsers(query) {
      if (query !== '') {
        this.userLoading = true
        listUser({ nickName: query, pageNum: 1, pageSize: 20 }).then(response => {
          this.userList = response.rows
          this.userLoading = false
        }).catch(() => {
          this.userLoading = false
        })
      } else {
        this.userList = []
      }
    },
    // 查看详情
    handleView(row) {
      this.dialogTitle = '查看住户信息'
      this.dialogButtonText = '关闭'
      this.isReadOnly = true
      getHouseholdById(row.householdId).then(response => {
        this.form = response.data
        // 加载用户信息
        if (this.form.userId) {
          listUser({ userId: this.form.userId, pageNum: 1, pageSize: 1 }).then(res => {
            this.userList = res.rows
          })
        }
        this.dialogVisible = true
      })
    },
    // 新增住户
    handleAdd() {
      this.isReadOnly = false
      this.dialogTitle = '新增住户'
      this.dialogButtonText = '确定'
      this.resetForm()
      this.dialogVisible = true
    },
    // 编辑住户
    handleEdit(row) {
      this.isReadOnly = false
      this.dialogTitle = '编辑住户信息'
      this.dialogButtonText = '更新'
      getHouseholdById(row.householdId).then(response => {
        this.form = response.data
        // 加载用户信息
        if (this.form.userId) {
          listUser({ userId: this.form.userId, pageNum: 1, pageSize: 1 }).then(res => {
            this.userList = res.rows
          })
        }
        this.dialogVisible = true
      })
    },
    // 删除住户
    handleDelete(row) {
      this.$confirm('确认删除该住户信息吗？删除后将无法恢复！', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteHousehold(row.householdId).then(() => {
          this.$message.success('删除成功')
          this.fetchHouseholdList()
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
      this.$confirm('确认删除选中的住户信息吗？删除后将无法恢复！', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteHouseholdBatch(this.ids).then(() => {
          this.$message.success('批量删除成功')
          this.fetchHouseholdList()
        })
      }).catch(() => {
        this.$message.info('已取消删除')
      })
    },
    // 状态修改
    handleStatusChange(row) {
      const text = row.status === 0 ? '启用' : '停用'
      this.$confirm('确认要' + text + '该住户吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        updateHouseholdStatus(row.householdId, row.status).then(() => {
          this.$message.success(text + '成功')
        })
      }).catch(() => {
        row.status = row.status === 0 ? 1 : 0
      })
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.householdId)
      this.multiple = !selection.length
    },
    // 关闭对话框
    handleCloseDialog() {
      this.dialogVisible = false
      this.resetForm()
    },
    // 重置表单
    resetForm() {
      this.form = {
        householdId: undefined,
        userId: undefined,
        address: '',
        community: '',
        familySize: 1,
        meterNo: '',
        status: 0,
        remark: ''
      }
      this.userList = []
      if (this.$refs.householdForm) {
        this.$refs.householdForm.resetFields()
      }
    },
    // 提交表单
    handleSubmit() {
      this.$refs.householdForm.validate((valid) => {
        if (valid) {
          if (this.form.householdId) {
            updateHousehold(this.form).then(() => {
              this.$message.success('更新成功')
              this.dialogVisible = false
              this.resetForm()
              this.fetchHouseholdList()
            })
          } else {
            addHousehold(this.form).then(() => {
              this.$message.success('新增成功')
              this.dialogVisible = false
              this.resetForm()
              this.fetchHouseholdList()
            })
          }
        } else {
          this.$message.error('请填写必填项')
          return false
        }
      })
    },
    // 搜索按钮操作
    handleQuery() {
      this.queryParams.pageNum = 1
      this.fetchHouseholdList()
    },
    // 重置按钮操作
    resetQuery() {
      this.queryParams = {
        pageNum: 1,
        pageSize: 10,
        community: undefined,
        address: undefined,
        meterNo: undefined,
        status: undefined
      }
      this.handleQuery()
    },
    // 格式化日期时间
    formatDateTime(datetime) {
      if (!datetime) return ''
      return datetime
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

