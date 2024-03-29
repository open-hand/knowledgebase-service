# 更新日志
这个文件记录knowledgebase-service所有版本的重大变动。


## [1.1.0-alpha] - 2021-09-18

### 改变

- 优化编辑文档时按钮显示顺序。
 

### 修复

- 修复Windows系统文档字体显示问题。



## [1.0.0] - 2021-06-18

### 改变

- 优化创建文档可以选择创建到根目录。

### 修复

- 修复知识库文档多层级创建时，目录无法左右滚动。
- 修复知识库全屏切换/退出丢失编辑内容。



## [0.24.0] - 2020-12-24

### 改变

- 文章保存失败时不清空内容。



## [0.21.0] - 2020-03-06

### 新增

- 支持创建多个知识库。
- 支持知识库设置公开范围。
- 知识库支持设置文档模板。
- 支持基于模板创建知识库或者文档。
- 知识库支持复制文档。
- 知识库支持移动文档。
- 支持从回收站恢复知识库。

### 改变

- 部分页面样式优化。
- 部分报表优化。

### 修复

- 修复知识库全屏显示菜单栏的问题。
- 修复由于wiki迁移至知识库造成的操作历史、版本对比显示异常的问题。

## [0.20.0] - 2019-12-20

### 新增

- 支持查看最近的知识活动
- 支持回收站功能

### 改变

- 子任务支持关联知识
- 去除侧栏顶部文字说明
- 字段解释icon、说明样式统一
- 统一字体颜色以及字号大小

### 修复

- 修复问题详情知识链接跳转问题
- 修复知识库部分白页的情况


## [0.19.0] - 2019-10-18

### 新增

- 支持文档移动：单个文档移动和父级文档移动
- 支持文档定时保存为草稿，异常退出可恢复
- 支持文档设置个人的默认编辑模式
- 支持文档全文检索，按照关键词权重返回结果列表
- 项目层可以查看组织层的文档
- 文档支持全屏查看和编辑
- 文档版本记录增加标题信息
- 首页增加查看最近更新的文档列表

### 改变

- 优化文档链接分享操作
- 文档加载性能优化

### 修复

- 修复删除文档的脏数据

## [0.18.0] - 2019-06-21

### 新增

- 支持版本回滚以及版本对比。
- wiki文章迁移到知识管理中。
- 文档可以进行链接的分享。
- 支持word的导入，导入后支持预览。
- 页面支持导出pdf。

### 改变

- 删除空间/页面/评论api调整权限。
- 文章的保存优化。
- 编辑处理的优化。

### 修复

- 修复保存文章时会将名称更改为上一篇文章名的bug。

## [0.17.0] - 2019-05-24

### 新增

- 新增组织、项目层`知识管理`菜单。
- 新增快速创建、编辑、删除文档。
- 文档支持`Markdown`和`所见即所得`两种编辑风格。
- 文档以树形结构展示，直接拖动排序。
- 文档进行附件上传下载、评论、日志的查看。
- 新增文档目录结构可查看。
