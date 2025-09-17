<template>
  <div class="profile-view-container">
    <!-- 移动星空背景到最外层容器内部的最前面 -->
    <div class="stars-background">
      <div v-for="n in 50" :key="n" class="star"></div>
      <div class="shooting-star"></div>
      <div class="shooting-star"></div>
      <div class="shooting-star"></div>
    </div>
    
    <div class="profile-view">
      <div class="profile-content">
        <div class="profile-header">
          <div class="avatar-container">
            <img src="https://img-hepingan.oss-cn-hangzhou.aliyuncs.com/page/a.jpg" alt="何平安" class="avatar" />
            <div class="avatar-glow"></div>
          </div>
          <h1 class="name">何平安</h1>
          <p class="title">真-全栈开发者 | 用代码铸造自己的赛博乌托邦</p>
        </div>
        
        <div class="profile-sections">
          <!-- 关于我 -->
          <div class="profile-section">
            <h2><el-icon><User /></el-icon> 关于我</h2>
            <div class="section-content">
              <p>为创造而生！为创造而失眠！我热爱一切新事物，祈求能用自己的双手创造出受人所爱，受人所用的项目；自己经常参加各种IT活动比赛，如黑客松 编程设计赛等，瞻仰圈子内的大佬大牛，成为他们，超越他们！有着不服输敢创敢拼的精神，对自己所热爱的事物有着十二分精神，并在B站分享着自己的经验或4000粉支持；有良好的团队协作能力，对于专业上的知识总是瞻仰前沿学习新技能，今年夏天参加了adventure黑客松，深刻感受到创造的魅力，让我更加对未知知识的探索充满兴趣和动力；对于我的技术栈我熟悉Java的各大热门框架和服务； 眼前的少年眼中燃着对社会的炽热期盼，仿佛有万千星辰在眸中闪烁 —— 他正翘首以盼一个珍贵的机会，好让青春在时代的画布上挥洒出更多滚烫的可能与绚烂的色彩！</p>
            </div>
          </div>
          
          <!-- 技术栈 -->
          <div class="profile-section">
            <h2><el-icon><Document /></el-icon> 技术栈</h2>
            <div class="section-content">
              <div class="tech-grid" :class="{ expanded: isTechExpanded }">
                <div v-for="(skill, index) in skills" :key="index" class="tech-item">
                  <div class="tech-icon" :style="{ backgroundColor: skill.bgColor || '#f5f5f5' }">
                    <img :src="skill.icon" :alt="skill.name" class="tech-logo" />
                  </div>
                  <div class="tech-info">
                    <div class="tech-name">{{ skill.name }}</div>
                    <el-tag size="small" :type="getTagType(skill.level)">{{ getProficiencyLabel(skill.level) }}</el-tag>
                  </div>
                </div>
              </div>
              <div class="expand-button-container">
                <el-button 
                  type="text" 
                  class="expand-toggle-btn" 
                  @click="toggleTechExpanded"
                >
                  <el-icon class="expand-icon" :class="{ rotated: isTechExpanded }">
                    <ArrowDown />
                  </el-icon>
                  {{ isTechExpanded ? '收起' : '展开全部' }}
                </el-button>
              </div>
            </div>
          </div>
          
          <!-- 项目经历 -->
          <div class="profile-section">
            <h2><el-icon><Collection /></el-icon> 代表项目</h2>
            <div class="section-content">
              <div class="project-card" @click="openProjectPreview('pass-assistant')">
                <h3>Pass Assistant <span class="project-date">2024.2 - 至今</span></h3>
                <div class="project-preview-badge">
                  <el-icon><Picture /></el-icon> 预览
                </div>
                <p>励志打造国内一流综合工具类网站，不断进步，集成多种AI工具、源码分享和技术交流功能，属于自己心中的乌托邦。集成了我毕生所学的多种技术</p>
                <div class="project-tech">
                  <el-tag size="small" type="info">Vue</el-tag>
                  <el-tag size="small" type="success">Java</el-tag>
                  <el-tag size="small" type="warning">Python</el-tag>
                  <el-tag size="small" type="danger">AI</el-tag>
                  <el-tag size="small" type="danger">Redis</el-tag>
                  <el-tag size="small" type="info">MySQL</el-tag>
                </div>
                <div class="project-highlights">
                  <div class="highlight-item">
                    <el-icon><Star /></el-icon>
                    <span>获得全高计设计赛国赛入围</span>
                  </div>
                  <div class="highlight-item">
                    <el-icon><User /></el-icon>
                    <span>拥有活跃用户社区</span>
                  </div>
                  <div class="highlight-item">
                    <el-icon><Connection /></el-icon>
                    <span>构建三端应用架构</span>
                  </div>
                </div>
              </div>
              
              <!-- 可以添加更多项目 -->
              <div class="project-card" @click="openProjectPreview('super-pass')">
                <h3>SuperPass <span class="project-date">2025.3 - 至今</span></h3>
                <div class="project-preview-badge">
                  <el-icon><Picture /></el-icon> 预览
                </div>
                <p>全新校园生活服小程序app，提供校园资讯、校园活动、校园二手交易、校园论坛(类校园墙),商务广告,特色等级头像框,管理员页面等多种功能。</p>
                <p>目前已推广至本校使用，并获得校方认可，目前正在推广至其他高校。</p>
                <div class="project-tech">
                  <el-tag size="small" type="success">Java</el-tag>
                  <el-tag size="small" type="warning">Uniapp</el-tag>
                  <el-tag size="small" type="danger">Redis</el-tag>
                  <el-tag size="small" type="info">MySQL</el-tag>
                  <el-tag size="small" type="success">SpringCloud</el-tag>
                </div>
                <div class="project-highlights">
                  <div class="highlight-item">
                    <el-icon><Star /></el-icon>
                    <span>已推广至本校使用</span>
                  </div>
                  <div class="highlight-item">
                    <el-icon><User /></el-icon>
                    <span>拥有校园学生私域流量</span>
                  </div>
                  <div class="highlight-item">
                    <el-icon><Connection /></el-icon>
                    <span>功能"花里胡哨"</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 实习经历 -->
          <div class="profile-section">
            <h2><el-icon><Briefcase /></el-icon> 工作兼职经历</h2>
            <div class="section-content">
              <div class="experience-timeline">
                <div class="experience-item">
                  <div class="experience-date">
                    <div class="date-badge">2025.04 - 2025.08</div>
                  </div>
                  <div class="experience-content">
                    <div class="experience-header">
                      <h3>浩原科技工作室(个体工商户)</h3>
                      <div class="company-name">工作室负责人</div>
                    </div>
                    <div class="experience-description">
                      <p>软件外包类工作室；作为浩原科技工作室的主要开发者和管理人，旗下已有50余人接单技术人，工作室接取项目金额超5万元;独
立开发出工作室接单小程序（微信搜索浩原科技）;开发中主要技术栈为Springboot+Uniapp或Vue，个人累计接取十几单项目的
定制开发，有数单进行团队合作开发，对项目需求能做到100%实现，练就了熟练的项目开发经验,并熟练使用linux环境和宝塔面
板部署项目；
</p>
                      <ul class="achievement-list">
                        <li> "四会会议app"：独立完成；使用Langchain4j框架集成了阿里云百炼大模型，将本地知识库嵌入模型支持RAG检索，加入了网
                          页解析MCP服务，使用mysql数据库作为会话记忆存储，项目架构使用Spring Cloud+Uniapp.</li>
                        <li> "医疗管理系统“：两人完成；使用spring cloud alibaba微服务框架并独立设计技术选型，主要负责vue框架的web管理端，
                          熟练运用Mybatis-plus快速完成crud部分，并使用Hanlp+Echarts实现数据可视化</li>
                      </ul>
                    </div>
                  </div>
                </div>
                
                <div class="experience-item">
                  <div class="experience-date">
                    <div class="date-badge">2024.04 - 2024.06</div>
                  </div>
                  <div class="experience-content">
                    <div class="experience-header">
                      <h3>重庆中景商业管理有限公司</h3>
                      <div class="company-name">项目制后端开发兼职</div>
                    </div>
                    <div class="experience-description">
                      <p>线上兼职工作，负责Java后端开发，参与“创投服务”和“众创AI”项目的开发。具备精通Java的能力，熟练使用SSM、Spring B
                        oot、MySQL、Redis，Uniapp等开发框架和环境.</p>
                      <ul class="achievement-list">
                        <li>在“创投服务”项目中，作为后端主要负责人，成功将单体项目拆解成微服务项目，通过高效的团队间沟通和分模块开发，深
                          入掌握了企业团队合作开发项目的流程和实际应用。</li>
                        <li>在“众创AI”项目中，负责使用Nacos进行服务注册与配置管理，统一管理众多配置，并作为后端Java的主要开发人员之一，
                          显著提升了项目的稳定性和效率。</li>
                      </ul>
                    </div>
                
                  </div>
                </div>

                <div class="experience-item">
                  <div class="experience-date">
                    <div class="date-badge">2024.01 - 2024.10</div>
                  </div>
                  <div class="experience-content">
                    <div class="experience-header">
                      <h3>北京瑞麟祥科技有限公司</h3>
                      <div class="company-name">技术合作支持</div>
                    </div>
                    <div class="experience-description">
                      <p>与朋友一起创的公司，作为社区的技术负责人，负责建设社区网站框架内容，编写网页内容，与社区其他人沟通交流，分享AI前沿
科技。社区用户超千人，开发出AI工具整合包网站(https://hepingan.top)和基于Springboot+Php的AIGC社区APP，励志打造国
内优质AI社区，并基于Python研发出智谱批量打标器接取AIGC炼丹师的需求</p>
                    </div>
                
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 获奖经历 -->
          <div class="profile-section">
            <h2><el-icon><Star /></el-icon> 获奖经历</h2>
            <div class="section-content">
              <div class="awards-container">
                <div class="award-item">
                  <div class="award-icon">🏆</div>
                  <div class="award-info">
                    <div class="award-title">蓝桥杯省赛</div>
                    <div class="award-level">二等奖</div>
                    <div class="award-date">2025年</div>
                  </div>
                </div>
                
                <div class="award-item">
                  <div class="award-icon">🏅</div>
                  <div class="award-info">
                    <div class="award-title">中国大学生计算机大赛</div>
                    <div class="award-level">省二(上推国赛)</div>
                    <div class="award-date">2024年</div>
                  </div>
                </div>
                
                <div class="award-item">
                  <div class="award-icon">🎖️</div>
                  <div class="award-info">
                    <div class="award-title">全国挑战杯大赛</div>
                    <div class="award-level">校一等奖(上推省赛)</div>
                    <div class="award-date">2025年</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 联系方式 -->
          <div class="profile-section">
            <h2><el-icon><ChatDotRound /></el-icon> 联系方式</h2>
            <div class="section-content contacts">
              <div class="contact-item">
                <el-icon><Message /></el-icon>
                <span>微信：hepinganheliuyi(添加请说明来意)</span>
              </div>
              <div class="contact-item">
                <el-icon><Link /></el-icon>
                <span>博客: <a href="https://ai.hepingan.top" target="_blank">天香园</a></span>
              </div>
              <!-- B站信息 -->
              <div class="contact-item bilibili-item">
                <svg class="bilibili-icon" viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg" width="20" height="20">
                  <path d="M777.514667 131.669333a53.333333 53.333333 0 0 1 0 75.434667L728.746667 255.829333h49.92A160 160 0 0 1 938.666667 415.872v320a160 160 0 0 1-160 160H245.333333A160 160 0 0 1 85.333333 735.872v-320a160 160 0 0 1 160-160h49.749334L246.4 207.146667a53.333333 53.333333 0 1 1 75.392-75.434667l113.152 113.152c3.370667 3.370667 6.186667 7.04 8.448 10.965333h137.088c2.261333-3.925333 5.12-7.68 8.490667-11.008l113.109333-113.152a53.333333 53.333333 0 0 1 75.434667 0z m1.152 231.253334H245.333333a53.333333 53.333333 0 0 0-53.205333 49.365333L192 415.872v320a53.333333 53.333333 0 0 0 49.365333 53.205333L245.333333 789.333333h533.333334a53.333333 53.333333 0 0 0 53.205333-49.365333L832 735.872v-320a53.333333 53.333333 0 0 0-53.333333-53.333333z m-426.666667 106.666666c29.44 0 53.333333 23.893333 53.333333 53.333334v53.333333a53.333333 53.333333 0 1 1-106.666666 0v-53.333333c0-29.44 23.893333-53.333333 53.333333-53.333334z m320 0c29.44 0 53.333333 23.893333 53.333333 53.333334v53.333333a53.333333 53.333333 0 1 1-106.666666 0v-53.333333c0-29.44 23.893333-53.333333 53.333333-53.333334z" fill="currentColor"/>
                </svg>
                <span>
                  <a href="https://space.bilibili.com/443081814" target="_blank" class="bilibili-link">
                    B站: 何平安啦 
                    <div class="bilibili-stats">
                      <el-tooltip effect="light" placement="top">
                        <template #content>实时粉丝数</template>
                        <span v-if="bilibiliStats.followers !== null" class="stat-item">
                          <span class="bilibili-stat-text">粉丝数</span>
                          <span class="bilibili-stat-num" :title="formatNumberWithComma(bilibiliStats.followers)">
                            {{ formatNumber(bilibiliStats.followers) }}
                          </span>
                          <el-button 
                            type="text" 
                            class="refresh-btn" 
                            size="small" 
                            :loading="bilibiliStats.loading"
                            @click.stop="fetchBilibiliStats"
                          >
                            <i class="el-icon-refresh"></i>
                          </el-button>
                        </span>
                        <span v-else class="stat-item loading">
                          <i class="el-icon-loading"></i> 加载中...
                        </span>
                      </el-tooltip>
                    </div>
                  </a>
                </span>
              </div>
              <!-- 可以添加更多联系方式 -->
            </div>
          </div>
        </div>
        
        <div class="back-button-container">
          <el-button type="primary" @click="goBack" class="back-button">
            <el-icon><Back /></el-icon>
            返回
          </el-button>
        </div>
      </div>
    </div>
  </div>
  
  <!-- 项目预览对话框 -->
  <el-dialog
    v-model="previewDialogVisible"
    :title="currentProject?.name + ' 项目预览'"
    width="80%"
    destroy-on-close
    class="project-preview-dialog"
    top="5vh"
  >
    <div class="project-preview-container">
      <el-carousel v-if="currentProject" :interval="5000" type="card" height="500px" :autoplay="false">
        <el-carousel-item v-for="(image, index) in currentProject.images" :key="index">
          <div class="carousel-image-container">
            <img :src="image.url" :alt="image.caption" class="carousel-image">
            <div class="carousel-caption">{{ image.caption }}</div>
          </div>
        </el-carousel-item>
      </el-carousel>
      <div class="preview-description">
        <div v-if="currentProject?.id === 'pass-assistant'" class="project-description">
          <div class="description-section">
            <h4>💡 项目简介</h4>
            <p>
              一款不断进步的AIGC+工具类网站，该项目已上线并稳定运营近2年，该网站集合了AI工具和服务管理等十余种功能模块，如AI对
话，AI绘画，源码交易市场平台，外链推荐模块，在线生成工具模块，图床模块，打赏模块等。项目有管理员后台管理功能，支持
支付宝打赏和交易，页面设计现代简约又有未来感。且预设了数种预设角色，AI绘画功能也是可以随时切换绘画模型，实现一个网
站上使用多个网站的内容；源码市场模块提供给程序员一个成果变现的平台。
            </p>
          </div>
          
          <div class="description-section architecture-section">
            <h4>🏗️ 技术架构</h4>
            <div class="architecture-content">
              <div class="arch-layer">
                <span class="layer-title">技术栈：</span>
                <span class="layer-desc">前端：Vue.js 3 + Element Plus + SCSS，响应式设计，支持PWA;<br/>
                  后端：springcloud alibaba 微服务架构+Springboot + Mybatis-plus + Redis + Mysql + Hadoop + Docker + Nginx + SSL + 
                  sa-token + 多模型集成（GPT、GLM、Deepseek等）+Spring ai + Langchain4j(曾使用过)
                </span>
              </div>
              <div class="arch-layer">
                <span class="layer-title">关键技术点：</span>
                <span class="layer-desc">
 •采用 springcloud alibaba 微服务架构，独立从原有springboot单体架构拆解成微服务架构并自行进行架构设计，并完成nacos,
 gateway,熔断与降级等配置，大幅提升服务的稳定和高并发性，引用了优秀maven库，如 fastjson, hadoop, mybatis-plus组件
等; 前端采用Vue3，使用前后端分离开发模式,Vue中引入了Element, vuex等优秀组件;支持docker一键打包部署；<br/>
•ai对话从2024原有的传统ws请求升级到最新的spring ai1.0，提升了ai大模型的管理,知识库配置和MCP服务使用的便捷，真正实
现了多模态多模型+RAG检索+MCP服务可随时切换的ai对话平台；<br/>
•使用sa-token替代JWT令牌权限验证，并自行设计封装了sa-token-redis的工具类,增加开发效率和稳定性，并自行研究源码解决
了sa-token与微服务网关的版本问题<br/>
•数据库采用 mysql进行基础数据存储，还使用了 redis 对服务配置类信息存储，加快了用户在调用api时的速度且使用键值对存储
方便明了<br/>
•校园新闻采用Python的flask后台接口服务，利用Python爬虫的优势快速编写代码(现已废弃)。<br/>
•对话数据存储使用过Hadoop集群存储技术，缓解了海量数据在MySQL读取的压力和服务器的负载(现采用mysql简单存储).<br/>
 •采用企业级的开发规范，区分开发和上线环境，使用三层架构,封装工具类,每个方法写上注释,git管理规范该项目已参加2024年全
国计算机设计大赛软件类 web 应用开发的比赛进入全国决赛，参与对该项目的演示和答辩。站内用户有200余人，已稳定运营50
 0余天。服务次数1w+<br/></span>
              </div>
              <div class="arch-layer">
                <span class="layer-title">开源地址：</span>
                <span class="layer-desc">https://gitee.com/hepingan11/gptjava  (Tips:后端有单体架构和微服务架构两种，微服务在cloud分支)</span>
              </div>
            </div>
          </div>
          
          <div class="description-section features-section">
            <h4>⭐ 核心特性</h4>
            <div class="features-grid">
              <div class="feature-item">
                <span class="feature-icon">🤖</span>
                <span>多AI模型集成</span>
              </div>
              <div class="feature-item">
                <span class="feature-icon">📁</span>
                <span>知识库问答</span>
              </div>
              <div class="feature-item">
                <span class="feature-icon">🔧</span>
                <span>MCP协议支持</span>
              </div>
              <div class="feature-item">
                <span class="feature-icon">💬</span>
                <span>实时对话</span>
              </div>
              <div class="feature-item">
                <span class="feature-icon">📤</span>
                <span>文件上传处理</span>
              </div>
              <div class="feature-item">
                <span class="feature-icon">🌐</span>
                <span>多端适配</span>
              </div>
              <div class="feature-item">
                <span class="feature-icon">💰</span>
                <span>源码交易市场</span>
              </div>
            </div>
          </div>
        </div>
        
        <div v-else-if="currentProject?.id === 'super-pass'" class="project-description">
          <div class="description-section">
            <h4>💡 项目简介</h4>
            <p>
              不断更新的校园二手交易+论坛app，其年轻化的页面设计和趣味的等级头像框等功能吸引力年轻大学生使用；该项目主要有类闲
鱼的二手交易功能，类贴吧的校园墙论坛模块，包含完美的评论交易对话审核举报crud和管理端等功能, 还将上线AI工具模块方便
用户快捷使用,项目还支持docker一键部署。<br/>
目前项目得到我校老师支持，在本校开始试运营中
            </p>
          </div>
          
          <div class="description-section architecture-section">
            <h4>🏗️ 技术架构</h4>
            <div class="architecture-content">
              <div class="arch-layer">
                <span class="layer-title">技术栈：</span>
                <span class="layer-desc">前端：UniApp跨平台框架，支持微信小程序、H5、APP<br/>
                  后端：Springboot框架+Uniapp框架前后端分离开发模块，引入了Sa-token登录验证, 阿里Oss存储, Mybatis-plus等优秀maven库<br/>
                  数据库：Mysql+Redis</span>
              </div>
              <div class="arch-layer">
                <span class="layer-title">关键技术点：</span>
                <span class="layer-desc">•项目采用Springboot框架+Uniapp框架前后端分离开发模块，引入了Sa-token登录验证, 阿里Oss存储, Mybatis-plus等优秀ma
 ven库<br/>
•使用RabbitMQ消息队列对用户注册后的默认信息设置，头像框信息等加入队列中，减少用户在注册成功后的等待时间<br/>
•使用Mysql+Redis数据库进行配合存储，如用户登录信息存在Redis中<br/>
•项目接入了微信官方内容安全识别api, Ai对话Ai绘画api等<br/>
•项目使用Elasticsearch搜索技术替代原有sql查询提高文章商品搜索效率<br/></span>
              </div>
              <div class="arch-layer">
                <span class="layer-title">开源地址：</span>
                <span class="layer-desc">https://gitee.com/hepingan11/superpass-app</span>
              </div>
            </div>
          </div>
          
          <div class="description-section features-section">
            <h4>⭐ 核心功能</h4>
            <div class="features-grid">
              <div class="feature-item">
                <span class="feature-icon">📰</span>
                <span>校园资讯</span>
              </div>
              <div class="feature-item">
                <span class="feature-icon">🎉</span>
                <span>活动管理</span>
              </div>
              <div class="feature-item">
                <span class="feature-icon">🛒</span>
                <span>二手交易</span>
              </div>
              <div class="feature-item">
                <span class="feature-icon">💬</span>
                <span>校园论坛</span>
              </div>
              <div class="feature-item">
                <span class="feature-icon">📢</span>
                <span>商务广告</span>
              </div>
              <div class="feature-item">
                <span class="feature-icon">👑</span>
                <span>等级头像框</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="previewDialogVisible = false">关闭</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue';
import { useRouter } from 'vue-router';
import { User, Document, Collection, Star, ChatDotRound, Message, Link, Connection, Back, Picture, ArrowDown, ArrowUp, Briefcase } from '@element-plus/icons-vue';
import { getBilibiliData } from '../../api/BSideApi';

const router = useRouter();

// 项目预览图数据
const projectsImages = ref([
  {
    id: 'pass-assistant',
    name: 'Pass Assistant',
    images: [
      { url: 'https://img-hepingan.oss-cn-hangzhou.aliyuncs.com/PixPin_2024-06-11_15-10-24.png', caption: 'Pass Assistant 首页' },
      { url: 'https://img-hepingan.oss-cn-hangzhou.aliyuncs.com/page/QQ20250501-211214.png', caption: 'AI 工具页面' },
      { url: 'https://img-hepingan.oss-cn-hangzhou.aliyuncs.com/page/QQ20250506-123348.png', caption: '源码星球模块' },
      { url: 'https://img-hepingan.oss-cn-hangzhou.aliyuncs.com/page/20250817035652.png', caption: 'AI对话模块' }
    ]
  },
  {
    id: 'super-pass',
    name: 'SuperPass',
    images: [
      { url: 'https://img-hepingan.oss-cn-hangzhou.aliyuncs.com/page/52bb70bd663efac8bb4be2c3e6b8fe30.jpg', caption: 'SuperPass介绍' },
      { url: 'https://img-hepingan.oss-cn-hangzhou.aliyuncs.com/page/SrfVQq3m.png', caption: '在线体验' },
      { url: 'https://img-hepingan.oss-cn-hangzhou.aliyuncs.com/page/QQ20250608-182631.png', caption: '首页预览' },
      { url: 'https://img-hepingan.oss-cn-hangzhou.aliyuncs.com/page/QQ20250608-182623.png', caption: '论坛模块' },
      { url: 'https://img-hepingan.oss-cn-hangzhou.aliyuncs.com/page/QQ20250610-183251.png', caption: '评论拦截展示' },
    ]
  }
]);

// 控制预览图对话框的显示
const previewDialogVisible = ref(false);
// 当前选择的项目
const currentProject = ref(null);

// 控制技术栈展开状态
const isTechExpanded = ref(false);

// 打开项目预览对话框
const openProjectPreview = (projectId) => {
  const project = projectsImages.value.find(p => p.id === projectId);
  if (project) {
    currentProject.value = project;
    previewDialogVisible.value = true;
  }
};

// 切换技术栈展开状态
const toggleTechExpanded = () => {
  isTechExpanded.value = !isTechExpanded.value;
};

// B站数据
const bilibiliStats = ref({
  followers: null,
  loading: true,
  error: null
});

// 格式化数字（大于10000显示为x.x万）
const formatNumber = (num) => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + '万';
  }
  return num.toString();
};

// 格式化数字（带千位分隔符）
const formatNumberWithComma = (num) => {
  return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',');
};

// 获取B站数据
const fetchBilibiliStats = async () => {
  // 设置加载状态
  bilibiliStats.value.loading = true;
  const res = await getBilibiliData();
  console.log(res);
  bilibiliStats.value.loading = false;
  bilibiliStats.value.followers = res;
};

// 用于存储定时器ID
let bilibiliStatsTimer = null;

// 页面挂载时获取B站数据
onMounted(() => {
  fetchBilibiliStats();
});

// 组件卸载前清除定时器
onBeforeUnmount(() => {
  if (bilibiliStatsTimer) {
    clearInterval(bilibiliStatsTimer);
    bilibiliStatsTimer = null;
  }
});

// 技能数据
const skills = [
  { 
    name: 'Vue.js', 
    level: 90, 
    color: '#42b883',
    icon: 'https://img-hepingan.oss-cn-hangzhou.aliyuncs.com/page/Vue.svg',
    bgColor: 'rgba(66, 184, 131, 0.1)'
  },
  { 
    name: 'Java', 
    level: 90, 
    color: '#f89820',
    icon: 'https://img-hepingan.oss-cn-hangzhou.aliyuncs.com/page/java.svg',
    bgColor: 'rgba(248, 152, 32, 0.1)'
  },
  { 
    name: 'Python', 
    level: 85, 
    color: '#306998',
    icon: 'https://img-hepingan.oss-cn-hangzhou.aliyuncs.com/page/Python.svg',
    bgColor: 'rgba(48, 105, 152, 0.1)'
  },
  { 
    name: 'Spring Boot', 
    level: 90, 
    color: '#6db33f',
    icon: 'https://img-hepingan.oss-cn-hangzhou.aliyuncs.com/page/springboot.svg',
    bgColor: 'rgba(109, 179, 63, 0.1)'
  },
  { 
    name: 'Uniapp ', 
    level: 90,
    color: '#e34c26',
    icon: 'https://img-hepingan.oss-cn-hangzhou.aliyuncs.com/page/uniapp.svg',
    bgColor: 'rgba(65, 184, 131, 0.1)'
  },
  { 
    name: 'MySQL', 
    level: 80, 
    color: '#4479a1',
    icon: 'https://img-hepingan.oss-cn-hangzhou.aliyuncs.com/page/mysql.svg',
    bgColor: 'rgba(68, 121, 161, 0.1)'
  },
  { 
    name: 'Redis', 
    level: 80, 
    color: '#c7254e',
    icon: 'https://cdn.jsdelivr.net/gh/devicons/devicon/icons/redis/redis-original.svg',
    bgColor: 'rgba(199, 37, 78, 0.1)'
  },
  { 
    name: 'JavaScript', 
    level: 88, 
    color: '#f7df1e',
    icon: 'https://cdn.jsdelivr.net/gh/devicons/devicon/icons/javascript/javascript-original.svg',
    bgColor: 'rgba(247, 223, 30, 0.1)'
  },
  { 
    name: 'CSS/SCSS', 
    level: 85, 
    color: '#264de4',
    icon: 'https://cdn.jsdelivr.net/gh/devicons/devicon/icons/css3/css3-original.svg',
    bgColor: 'rgba(38, 77, 228, 0.1)'
  },
  { 
    name: 'Docker', 
    level: 80, 
    color: '#2496ed',
    icon: 'https://cdn.jsdelivr.net/gh/devicons/devicon/icons/docker/docker-original.svg',
    bgColor: 'rgba(36, 150, 237, 0.1)'
  },
  {
    name: 'n8n', 
    level: 80, 
    color: '#264de4',
    icon: 'https://img-hepingan.oss-cn-hangzhou.aliyuncs.com/page/n8n-color.svg',
    bgColor: 'rgba(205, 164, 249, 0.2)'
  },
  {
    name: 'Spring Cloud', 
    level: 85, 
    color: '#2496ed',
    icon: 'https://img-hepingan.oss-cn-hangzhou.aliyuncs.com/page/SPRINGCLOUD.svg',
    bgColor: 'rgba(110, 100, 107, 0.1)'
  },
  { 
    name: 'Linux', 
    level: 70, 
    color: '#003366',
    icon: 'https://cdn.jsdelivr.net/gh/devicons/devicon/icons/linux/linux-original.svg',
    bgColor: 'rgba(0, 51, 102, 0.1)'
  },
  { 
    name: 'Git', 
    level: 80, 
    color: '#f05032',
    icon: 'https://cdn.jsdelivr.net/gh/devicons/devicon/icons/git/git-original.svg',
    bgColor: 'rgba(240, 80, 50, 0.1)'
  },
  { 
    name: 'C/C++', 
    level: 70, 
    color: '#003366',
    icon: 'https://cdn.jsdelivr.net/gh/devicons/devicon/icons/cplusplus/cplusplus-original.svg',
    bgColor: 'rgba(0, 51, 102, 0.1)'
  },
  { 
    name: 'AE', 
    level: 70, 
    color: '#264de4',
    icon: 'https://img-hepingan.oss-cn-hangzhou.aliyuncs.com/page/Adobe%20Ae.svg',
    bgColor: 'rgba(205, 164, 249, 0.2)'
  },
  { 
    name: 'PS', 
    level: 70, 
    color: '#264de4',
    icon: 'https://cdn.jsdelivr.net/gh/devicons/devicon/icons/photoshop/photoshop-plain.svg',
    bgColor: 'rgba(150, 200, 255, 0.1)'
  },
  { 
    name: 'Blender  ', 
    level: 80, 
    color: '#264de4',
    icon: 'https://img-hepingan.oss-cn-hangzhou.aliyuncs.com/page/Blender.svg',
    bgColor: 'rgba(150, 200, 255, 0.1)'
  },
  {
    name: 'Hadoop',
    level: 70,
    color: '#264de4',
    icon: 'https://img-hepingan.oss-cn-hangzhou.aliyuncs.com/page/hadoop.svg',
    bgColor: 'rgba(150, 150, 1, 0.1)'
  },
  {
    name: 'RabbitMQ',
    level: 70,
    color: '#264de4',
    icon: 'https://img-hepingan.oss-cn-hangzhou.aliyuncs.com/page/rabbitmq.svg',
    bgColor: 'rgba(150, 200, 255, 0.1)'
  },
  {
    name: 'Elasticsearch',
    level: 70,
    color: '#264de4',
    icon: 'https://img-hepingan.oss-cn-hangzhou.aliyuncs.com/page/elasticsearch-Elasticsearch.svg',
    bgColor: 'rgba(150, 200, 255, 0.1)'
  },
  {
    name: 'Nacos',
    level: 80,
    color: '#264de4',
    icon: 'https://img-hepingan.oss-cn-hangzhou.aliyuncs.com/page/nacos.svg',
    bgColor: 'rgba(150, 200, 255, 0.1)'
  },
  { 
    name: 'STM32', 
    level: 60, 
    color: '#2496ed',
    icon: 'https://img-hepingan.oss-cn-hangzhou.aliyuncs.com/page/stm32%E7%A8%8B%E5%BA%8F%E5%BC%80%E5%8F%91.svg',
    bgColor: 'rgba(36, 150, 237, 0.1)'
  },
  { 
    name: 'Unity  ', 
    level: 60, 
    color: '#264de4',
    icon: 'https://img-hepingan.oss-cn-hangzhou.aliyuncs.com/page/unity.svg',
    bgColor: 'rgba(150, 200, 255, 0.1)'
  },
  {
    name: 'Flutter',
    level: 60,
    color: '#264de4',
    icon: 'https://img-hepingan.oss-cn-hangzhou.aliyuncs.com/page/Flutter.svg',
    bgColor: 'rgba(150, 200, 255, 0.1)'
  },
  {
    name: 'Spring AI',
    level: 90,
    color: '#264de4',
    icon: 'https://img-hepingan.oss-cn-hangzhou.aliyuncs.com/page/spring-copy.svg',
    bgColor: 'rgba(150, 150, 255, 0.1)'
  },
  {
    name: 'Langchain4j',
    level: 80,
    color: '#264de4',
    icon: 'https://docs.langchain4j.info/img/logo.svg',
    bgColor: 'rgba(50, 50, 250, 0.1)'
  },
  {
    name: 'Sa-token',
    level: 90,
    color: '#264de4',
    icon: 'https://sa-token.cc/logo.png',
    bgColor: 'rgba(150, 200, 255, 0.1)'
  },
  {
    name: 'MyBatis-Plus',
    level: 90,
    color: '#264de4',
    icon: 'https://baomidou.com/assets/asset.cIbiVTt__ZgvyzK.svg',
    bgColor: 'rgba(150, 200, 255, 0.1)'
  },
  {
    name: 'Cursor Coding',
    level: 100,
    color: '#264de4',
    icon: 'https://img-hepingan.oss-cn-hangzhou.aliyuncs.com/link/9145d6b6-8a0c-4268-ba6e-d24f881a0cea.jpg',
    bgColor: 'rgba(150, 200, 255, 0.1)'
  }
];

// 根据熟练度返回标签类型
const getTagType = (level) => {
  if (level >= 90) return 'danger'; // 精通
  if (level >= 80) return 'warning'; // 熟练
  if (level >= 70) return 'success'; // 掌握
  return 'info'; // 了解
};

// 根据熟练度返回标签文本
const getProficiencyLabel = (level) => {
  if (level >= 90) return '精通';
  if (level >= 80) return '熟练';
  if (level >= 70) return '掌握';
  return '了解';
};

// 返回上一页
const goBack = () => {
  router.back();
};
</script>

<style lang="scss" scoped>
.profile-view-container {
  width: 100%;
  height: 100vh;
  overflow-y: auto; /* 容器级别的滚动 */
  position: relative;
  background: linear-gradient(to bottom, #020111 10%, #20202c 30%, #252538 60%, #3a3a5c 90%);
}

.profile-view {
  position: relative;
  background-color: transparent; /* 将背景改为透明 */
  color: var(--textColor1);
  min-height: 100%;
  height: auto;
  width: 100%;
  z-index: 1; /* 确保内容在星星上方 */
}

/* 移除Sparkles背景相关样式 */

/* 内容样式 */
.profile-content {
  position: relative;
  max-width: 900px;
  margin: 0 auto;
  padding: 60px 20px 100px 20px; /* 增加底部padding */
}

.profile-header {
  text-align: center;
  margin-bottom: 60px;
  animation: fadeInDown 0.8s ease;
}

.avatar-container {
  position: relative;
  width: 160px;
  height: 160px;
  margin: 0 auto 20px;
}

.avatar {
  width: 150px;
  height: 150px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid var(--themeColor1);
  position: relative;
  z-index: 1;
  transition: transform 0.6s ease;
  cursor: pointer;
  
  &:hover {
    transform: rotate(360deg);
  }
}

.avatar-glow {
  position: absolute;
  top: -5px;
  left: -5px;
  right: -5px;
  bottom: -5px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--themeColor1), var(--themeColor2));
  opacity: 0.6;
  filter: blur(8px);
  animation: pulse 3s infinite ease-in-out;
  z-index: 0;
}

.name {
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 10px;
  background: linear-gradient(135deg, #8A2387, #E94057, #F27121);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  color: transparent;
}

.title {
  font-size: 18px;
  color: var(--textColor2);
  margin: 0;
  opacity: 0.8;
}

.profile-sections {
  display: grid;
  grid-template-columns: 1fr;
  gap: 40px;
}

.profile-section {
  background-color: rgba(var(--bgColor2-rgb, 35, 35, 40), 0.7) !important;
  backdrop-filter: blur(10px) !important;
  border-radius: 16px;
  padding: 25px;
  border: 1px solid rgba(255, 255, 255, 0.05);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  animation: fadeIn 0.8s ease;
  
  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 15px 40px rgba(0, 0, 0, 0.2);
  }
  
  h2 {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-top: 0;
    margin-bottom: 20px;
    font-size: 22px;
    color: var(--themeColor1);
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
    padding-bottom: 10px;
    
    .el-icon {
      color: var(--themeColor1);
    }
  }
}

.section-content {
  line-height: 1.7;
  color: var(--textColor2);
  
  p {
    margin: 10px 0;
  }
}

.tech-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 20px;
  max-height: 450px; /* 限制高度，大约显示2行 */
  overflow: hidden;
  transition: all 0.6s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  
  &::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    height: 60px;
    background: linear-gradient(transparent, rgba(var(--bgColor2-rgb, 35, 35, 40), 0.95));
    pointer-events: none;
    transition: all 0.6s cubic-bezier(0.4, 0, 0.2, 1);
    opacity: 1;
  }
  
  &.expanded {
    max-height: 2000px; /* 足够大的值确保完全展开 */
    
    &::after {
      opacity: 0;
      transform: translateY(20px);
    }
  }
}

.tech-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  transition: transform 0.3s ease;
  padding: 15px 10px;
  border-radius: 12px;
  background-color: rgba(255, 255, 255, 0.05);
  
  &:hover {
    transform: translateY(-5px);
    background-color: rgba(255, 255, 255, 0.1);
  }
}

.tech-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12px;
  padding: 8px;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  
  &:hover {
    transform: scale(1.1);
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
  }
}

.tech-logo {
  width: 85%;
  height: 85%;
  object-fit: contain;
}

.tech-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  width: 100%;
}

.tech-name {
  font-weight: 500;
  margin-bottom: 5px;
  color: var(--textColor1);
}

.expand-button-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  padding-top: 15px;
  border-top: 1px solid rgba(255, 255, 255, 0.05);
}

.expand-toggle-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--textColor2);
  font-size: 14px;
  padding: 10px 20px;
  border-radius: 25px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.05), rgba(255, 255, 255, 0.1));
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  backdrop-filter: blur(10px);
  
  &:hover {
    color: var(--themeColor1);
    background: linear-gradient(135deg, rgba(var(--themeColor1-rgb, 64, 158, 255), 0.1), rgba(var(--themeColor1-rgb, 64, 158, 255), 0.2));
    border-color: rgba(var(--themeColor1-rgb, 64, 158, 255), 0.3);
    transform: translateY(-3px);
    box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
  }
  
  .expand-icon {
    font-size: 16px;
    transition: all 0.6s cubic-bezier(0.4, 0, 0.2, 1);
    
    &.rotated {
      transform: rotate(180deg);
    }
  }
}

.project-card {
  padding: 20px;
  background-color: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  margin-bottom: 20px;
  position: relative;
  cursor: pointer;
  transition: all 0.3s ease;
  overflow: hidden;
  
  &:hover {
    transform: translateY(-5px);
    background-color: rgba(255, 255, 255, 0.1);
    box-shadow: 0 15px 30px rgba(0, 0, 0, 0.2);
  }
  
  h3 {
    margin-top: 0;
    margin-bottom: 10px;
    color: var(--textColor1);
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-wrap: nowrap;
  }
  
  .project-date {
    font-size: 14px;
    font-weight: normal;
    color: var(--textColor3);
    white-space: nowrap;
    margin-left: 10px;
  }
  
  p {
    margin: 10px 0;
  }
  
  .project-tech {
    display: flex;
    gap: 8px;
    margin: 15px 0;
    flex-wrap: wrap;
  }
}

.project-preview-badge {
  position: relative;
  background-color: var(--themeColor1);
  color: #fff;
  padding: 5px 10px;
  border-radius: 20px;
  font-size: 12px;
  display: inline-flex;
  align-items: center;
  gap: 5px;
  margin-bottom: 10px;
  transition: all 0.3s ease;
  
  .el-icon {
    font-size: 14px;
  }
  
  .project-card:hover & {
    background-color: var(--themeColor2);
    transform: translateY(-2px);
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
  }
}

/* 项目预览对话框样式 */
.project-preview-dialog {
  :deep(.el-dialog__header) {
    font-weight: bold;
    border-bottom: 1px solid rgba(255, 255, 255, 0.15);
    padding-bottom: 15px;
    margin-bottom: 20px;
    color: #fff;
    background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  }
  
  :deep(.el-dialog__body) {
    padding: 10px 20px 30px;
    background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
    color: #fff;
  }
  
  :deep(.el-dialog__footer) {
    background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
    border-top: 1px solid rgba(255, 255, 255, 0.1);
    padding-top: 15px;
  }
  
  :deep(.el-dialog) {
    background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
    border-radius: 16px;
    box-shadow: 0 20px 50px rgba(0, 0, 0, 0.7);
    border: 1px solid rgba(64, 158, 255, 0.2);
    overflow: hidden;
    backdrop-filter: blur(15px);
  }
  
  :deep(.el-dialog__title) {
    color: #fff;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  }
  
  :deep(.el-dialog__headerbtn .el-dialog__close) {
    color: rgba(255, 255, 255, 0.8);
    
    &:hover {
      color: var(--themeColor1);
      transform: scale(1.1);
    }
  }
}

.project-preview-container {
  .el-carousel {
    margin-bottom: 20px;
    
    :deep(.el-carousel__indicators) {
      .el-carousel__button {
        background-color: rgba(var(--themeColor1-rgb, 64, 158, 255), 0.8);
        border: 1px solid rgba(255, 255, 255, 0.2);
      }
      
      .el-carousel__indicator.is-active .el-carousel__button {
        background-color: var(--themeColor1);
        box-shadow: 0 0 10px rgba(var(--themeColor1-rgb, 64, 158, 255), 0.6);
      }
    }
    
    :deep(.el-carousel__arrow) {
      background-color: rgba(26, 26, 46, 0.8);
      border: 1px solid rgba(64, 158, 255, 0.3);
      
      &:hover {
        background-color: var(--themeColor1);
        border-color: var(--themeColor1);
        transform: scale(1.1);
      }
    }
    
    :deep(.el-carousel__item) {
      border-radius: 12px;
      overflow: hidden;
      background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
      border: 1px solid rgba(64, 158, 255, 0.2);
      
      &.is-active {
        box-shadow: 0 0 30px rgba(var(--themeColor1-rgb, 64, 158, 255), 0.5);
        border-color: rgba(64, 158, 255, 0.4);
      }
    }
  }
}

.carousel-image-container {
  width: 100%;
  height: 100%;
  position: relative;
  border-radius: 8px;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  border: 1px solid rgba(64, 158, 255, 0.15);
}

.carousel-image {
  max-width: 100%;
  max-height: 90%;
  object-fit: contain;
  border-radius: 4px;
}

.carousel-caption {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(135deg, rgba(26, 26, 46, 0.9) 0%, rgba(22, 33, 62, 0.9) 100%);
  color: #fff;
  padding: 12px 15px;
  text-align: center;
  font-size: 16px;
  border-top: 1px solid rgba(64, 158, 255, 0.3);
  backdrop-filter: blur(8px);
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.5);
}

.preview-description {
  color: #fff;
  line-height: 1.6;
  max-width: 900px;
  margin: 30px auto 10px;
  background: linear-gradient(135deg, rgba(26, 26, 46, 0.4) 0%, rgba(22, 33, 62, 0.4) 100%);
  padding: 25px;
  border-radius: 12px;
  border: 1px solid rgba(64, 158, 255, 0.2);
  backdrop-filter: blur(10px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.3);
  
  p {
    margin: 0;
    color: rgba(255, 255, 255, 0.95);
    text-align: left;
  }
}

.project-description {
  display: flex;
  flex-direction: column;
  gap: 25px;
}

.description-section {
  background: linear-gradient(135deg, rgba(26, 26, 46, 0.3) 0%, rgba(22, 33, 62, 0.3) 100%);
  border-radius: 10px;
  padding: 20px;
  border: 1px solid rgba(64, 158, 255, 0.15);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
  
  h4 {
    color: var(--themeColor1);
    margin: 0 0 15px 0;
    font-size: 18px;
    font-weight: 600;
    display: flex;
    align-items: center;
    gap: 8px;
    text-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
  }
  
  p {
    margin: 0;
    color: rgba(255, 255, 255, 0.92);
    line-height: 1.7;
  }
}

.architecture-section {
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.05) 0%, rgba(129, 102, 231, 0.05) 100%);
  border-color: rgba(64, 158, 255, 0.2);
}

.architecture-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.arch-layer {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  
  &:last-child {
    border-bottom: none;
  }
  
  .layer-title {
    color: var(--themeColor1);
    font-weight: 600;
    min-width: 80px;
    flex-shrink: 0;
    font-size: 14px;
  }
  
  .layer-desc {
    color: rgba(255, 255, 255, 0.85);
    line-height: 1.5;
    font-size: 14px;
  }
}

.features-section {
  background: linear-gradient(135deg, rgba(233, 64, 87, 0.05) 0%, rgba(242, 113, 33, 0.05) 100%);
  border-color: rgba(233, 64, 87, 0.2);
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 15px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 15px;
  background: linear-gradient(135deg, rgba(26, 26, 46, 0.4) 0%, rgba(22, 33, 62, 0.4) 100%);
  border-radius: 8px;
  border: 1px solid rgba(64, 158, 255, 0.2);
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-2px);
    background: linear-gradient(135deg, rgba(26, 26, 46, 0.6) 0%, rgba(22, 33, 62, 0.6) 100%);
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.3);
    border-color: rgba(64, 158, 255, 0.4);
  }
  
  .feature-icon {
    font-size: 18px;
    flex-shrink: 0;
  }
  
  span:last-child {
    color: rgba(255, 255, 255, 0.95);
    font-size: 14px;
    font-weight: 500;
  }
}

@media (max-width: 768px) {
  .preview-description {
    padding: 20px;
    margin: 20px auto 10px;
  }
  
  .project-description {
    gap: 20px;
  }
  
  .description-section {
    padding: 15px;
    
    h4 {
      font-size: 16px;
    }
  }
  
  .arch-layer {
    flex-direction: column;
    gap: 8px;
    
    .layer-title {
      min-width: auto;
      font-size: 13px;
    }
    
    .layer-desc {
      font-size: 13px;
    }
  }
  
  .features-grid {
    grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
    gap: 12px;
  }
  
  .feature-item {
    padding: 10px 12px;
    
    .feature-icon {
      font-size: 16px;
    }
    
    span:last-child {
      font-size: 13px;
    }
  }
}

.project-highlights {
  margin-top: 15px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.highlight-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--textColor2);
  
  .el-icon {
    color: var(--themeColor1);
  }
}

.contacts {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 12px;
  
  .el-icon {
    color: var(--themeColor1);
  }
  
  a {
    color: var(--themeColor1);
    text-decoration: none;
    transition: opacity 0.3s;
    
    &:hover {
      opacity: 0.8;
      text-decoration: underline;
    }
  }
}

/* B站相关样式 */
.bilibili-item {
  .bilibili-icon {
    color: #fb7299;
    flex-shrink: 0;
    transition: transform 0.3s ease;
    margin-top: 2px;
  }
  
  &:hover .bilibili-icon {
    transform: scale(1.1);
  }
  
  .bilibili-link {
    display: flex;
    flex-direction: column;
    gap: 4px;
    
    &:hover {
      text-decoration: none;
    }
  }
  
  .bilibili-stats {
    display: flex;
    gap: 12px;
    margin-top: 4px;
    
    .stat-item {
      display: inline-flex;
      align-items: center;
      gap: 8px;
      font-size: 14px;
      background-color: rgba(251, 114, 153, 0.1);
      padding: 3px 10px;
      border-radius: 10px;
      color: #fb7299;
      transition: all 0.3s ease;
      
      &:hover {
        background-color: rgba(251, 114, 153, 0.2);
        transform: translateY(-2px);
      }
      
      &.loading {
        opacity: 0.6;
      }
      
      .bilibili-stat-text {
        font-size: 13px;
        color: var(--textColor2);
      }
      
      .bilibili-stat-num {
        font-weight: 600;
        color: #fb7299;
      }
      
      .refresh-btn {
        opacity: 0.6;
        padding: 0;
        margin-left: 4px;
        color: #fb7299;
        
        &:hover {
          opacity: 1;
          color: #fb7299;
        }
        
        .el-icon-refresh {
          font-size: 14px;
          transition: transform 0.3s ease;
        }
        
        &:hover .el-icon-refresh {
          transform: rotate(180deg);
        }
      }
    }
  }
}

.back-button-container {
  display: flex;
  justify-content: center;
  margin-top: 60px;
}

.back-button {
  display: flex;
  align-items: center;
  gap: 8px;
  background: linear-gradient(135deg, var(--themeColor1), var(--themeColor2));
  border: none;
  padding: 12px 30px;
  border-radius: 30px;
  transition: transform 0.3s, box-shadow 0.3s;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 10px 20px rgba(0, 0, 0, 0.2);
  }
  
  &:active {
    transform: translateY(0);
  }
}

/* Animations */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fadeInDown {
  from {
    opacity: 0;
    transform: translateY(-30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    opacity: 0.6;
  }
  50% {
    transform: scale(1.1);
    opacity: 0.8;
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .profile-content {
    padding: 40px 15px;
  }
  
  .profile-header {
    margin-bottom: 40px;
  }
  
  .avatar-container {
    width: 130px;
    height: 130px;
  }
  
  .avatar {
    width: 120px;
    height: 120px;
  }
  
  .name {
    font-size: 28px;
  }
  
  .title {
    font-size: 16px;
  }
  
  .tech-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 15px;
    max-height: 180px; /* 移动端调整高度 */
    
    &.expanded {
      max-height: 2000px;
    }
  }
  
  .expand-toggle-btn {
    padding: 8px 16px;
    font-size: 13px;
    
    .expand-icon {
      font-size: 14px;
    }
  }
  
  .project-highlights {
    grid-template-columns: 1fr;
  }
}

/* 奖项列表样式 */
.awards-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
}

.award-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  background-color: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-5px);
    background-color: rgba(255, 255, 255, 0.1);
    box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
  }
}

.award-icon {
  font-size: 30px;
  padding: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(var(--themeColor1-rgb, 64, 158, 255), 0.1);
  border-radius: 50%;
  width: 50px;
  height: 50px;
  flex-shrink: 0;
  transition: transform 0.3s ease;
  
  .award-item:hover & {
    transform: scale(1.1) rotate(10deg);
  }
}

.award-info {
  flex: 1;
}

.award-title {
  font-weight: 500;
  color: var(--textColor1);
  margin-bottom: 5px;
  font-size: 16px;
}

.award-level {
  color: var(--themeColor1);
  font-weight: 600;
  margin-bottom: 3px;
}

.award-date {
  font-size: 14px;
  color: var(--textColor3);
  opacity: 0.8;
}

/* 实习经历样式 */
.experience-timeline {
  position: relative;
  padding-left: 0;
}

.experience-item {
  display: flex;
  gap: 25px;
  margin-bottom: 35px;
  position: relative;
  
  &:last-child {
    margin-bottom: 0;
  }
  
  &::before {
    content: '';
    position: absolute;
    left: 85px;
    top: 40px;
    bottom: -35px;
    width: 2px;
    background: linear-gradient(135deg, var(--themeColor1), var(--themeColor2));
    border-radius: 2px;
  }
  
  &:last-child::before {
    display: none;
  }
  
  &::after {
    content: '';
    position: absolute;
    left: 78px;
    top: 32px;
    width: 16px;
    height: 16px;
    background: linear-gradient(135deg, var(--themeColor1), var(--themeColor2));
    border-radius: 50%;
    box-shadow: 0 0 0 4px rgba(var(--bgColor2-rgb, 35, 35, 40), 1), 0 0 0 6px rgba(var(--themeColor1-rgb, 64, 158, 255), 0.3);
  }
}

.experience-date {
  flex-shrink: 0;
  width: 140px;
  
  .date-badge {
    display: inline-block;
    background: linear-gradient(135deg, var(--themeColor1), var(--themeColor2));
    color: #fff;
    padding: 8px 15px;
    border-radius: 20px;
    font-size: 12px;
    font-weight: 600;
    text-align: center;
    box-shadow: 0 4px 12px rgba(var(--themeColor1-rgb, 64, 158, 255), 0.3);
    white-space: nowrap;
  }
}

.experience-content {
  flex: 1;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 16px;
  padding: 25px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 1px;
    background: linear-gradient(90deg, transparent, var(--themeColor1), transparent);
    opacity: 0;
    transition: opacity 0.3s ease;
  }
  
  &:hover {
    transform: translateY(-5px);
    background: rgba(255, 255, 255, 0.08);
    box-shadow: 0 15px 35px rgba(0, 0, 0, 0.15);
    border-color: rgba(var(--themeColor1-rgb, 64, 158, 255), 0.2);
    
    &::before {
      opacity: 1;
    }
  }
}

.experience-header {
  margin-bottom: 15px;
  
  h3 {
    margin: 0 0 8px 0;
    color: var(--textColor1);
    font-size: 18px;
    font-weight: 600;
  }
  
  .company-name {
    color: var(--themeColor1);
    font-size: 14px;
    font-weight: 500;
    opacity: 0.9;
  }
}

.experience-description {
  margin-bottom: 20px;
  
  p {
    margin: 0 0 15px 0;
    color: var(--textColor2);
    line-height: 1.6;
  }
  
  .achievement-list {
    margin: 0;
    padding-left: 20px;
    color: var(--textColor2);
    
    li {
      margin-bottom: 8px;
      line-height: 1.5;
      position: relative;
      
      &::marker {
        color: var(--themeColor1);
      }
      
      &:last-child {
        margin-bottom: 0;
      }
    }
  }
}

.experience-tech {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

@media (max-width: 768px) {
  .experience-item {
    flex-direction: column;
    gap: 15px;
    margin-bottom: 30px;
    
    &::before,
    &::after {
      display: none;
    }
  }
  
  .experience-date {
    width: auto;
    
    .date-badge {
      display: block;
      text-align: left;
      width: fit-content;
    }
  }
  
  .experience-content {
    padding: 20px;
    
    .experience-header h3 {
      font-size: 16px;
    }
  }
  
  .awards-container {
    grid-template-columns: 1fr;
  }
}

/* 星空背景特效 - 添加到样式的末尾 */
.stars-background {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0; 
  overflow: hidden;
  pointer-events: none;
}

.star {
  position: absolute;
  background-color: #fff;
  border-radius: 50%;
  box-shadow: 0 0 3px 1px rgba(255, 255, 255, 0.4);
}

@for $i from 1 through 50 {
  .star:nth-child(#{$i}) {
    width: #{(random(3) + 1)}px;
    height: #{(random(3) + 1)}px;
    left: #{random(100)}#{"%"};
    top: #{random(100)}#{"%"};
    opacity: #{(random(10) + 5) / 10};
    animation: twinkle #{(random(5) + 5)}s infinite ease-in-out #{random(10)}s;
  }
}

.shooting-star {
  position: absolute;
  height: 2px;
  background: linear-gradient(to right, rgba(255,255,255,0) 0%, rgba(255,255,255,1) 50%, rgba(255,255,255,0) 100%);
  opacity: 0;
  animation: shooting-star 8s infinite ease-out;
}

.shooting-star:nth-child(51) { 
  top: 20%; 
  left: 10%; 
  animation-delay: 2s; 
}

.shooting-star:nth-child(52) { 
  top: 50%; 
  left: 30%; 
  animation-delay: 5s; 
}

.shooting-star:nth-child(53) { 
  top: 70%; 
  left: 60%; 
  animation-delay: 8s; 
}

@keyframes twinkle {
  0%, 100% { opacity: 0.3; }
  50% { opacity: 1; }
}

@keyframes shooting-star {
  0% {
    transform: translateX(0) translateY(0) rotate(45deg);
    opacity: 0;
    width: 0;
  }
  5% {
    opacity: 1;
    width: 100px;
  }
  20% {
    width: 200px;
    opacity: 1;
  }
  30% {
    transform: translateX(300px) translateY(300px) rotate(45deg);
    opacity: 0;
    width: 0;
  }
  100% {
    opacity: 0;
  }
}

.dialog-footer {
  display: flex;
  justify-content: center;
  padding-top: 10px;
  
  .el-button {
    background: rgba(var(--themeColor1-rgb, 64, 158, 255), 0.3);
    border-color: var(--themeColor1);
    color: #fff;
    padding: 10px 25px;
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
      background: rgba(var(--themeColor1-rgb, 64, 158, 255), 0.5);
      border-color: var(--themeColor1);
    }
  }
}

.el-button {
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }
}
</style>

