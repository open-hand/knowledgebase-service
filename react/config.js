const config = {
  server: 'http://api.staging.saas.hand-china.com',
  // server: 'http://10.211.102.55:8080',
  master: './node_modules/@choerodon/master/lib/master.js',
  projectType: 'choerodon',
  buildType: 'single',
  dashboard: {},
  outward: '/knowledge/share',
};

module.exports = config;
