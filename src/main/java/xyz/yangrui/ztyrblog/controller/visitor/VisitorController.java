package xyz.yangrui.ztyrblog.controller.visitor;

import com.github.pagehelper.PageInfo;
import com.vdurmont.emoji.EmojiParser;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import xyz.yangrui.ztyrblog.constant.WebConst;
import xyz.yangrui.ztyrblog.dto.MetaDto;
import xyz.yangrui.ztyrblog.dto.ResultDTO;
import xyz.yangrui.ztyrblog.dto.Types;
import xyz.yangrui.ztyrblog.exception.TipException;
import xyz.yangrui.ztyrblog.modal.Bo.CommentBo;
import xyz.yangrui.ztyrblog.modal.Vo.CommentVo;
import xyz.yangrui.ztyrblog.modal.Vo.ContentVo;
import xyz.yangrui.ztyrblog.service.ICommentService;
import xyz.yangrui.ztyrblog.service.IContentService;
import xyz.yangrui.ztyrblog.service.IMetaService;
import xyz.yangrui.ztyrblog.service.ISiteService;
import xyz.yangrui.ztyrblog.utils.*;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.net.URLEncoder;

@RestController
@RequestMapping("fuckBolg")
@Api(tags = "访客界面", description = "访客界面相关接口")
public class VisitorController {
    private static final Logger LOGGER = LoggerFactory.getLogger(VisitorController.class);

    @Resource
    private IContentService contentService;

    @Resource
    private ICommentService commentService;

    @Resource
    private IMetaService metaService;

    @Resource
    private ISiteService siteService;

    protected MapCache cache = MapCache.single();

    @GetMapping(value = {"/", "index"})
    @ApiOperation(value = "首页-查询文章列表",notes = "查询第一页，默认返回12条数据")
    @ApiImplicitParam(name="limit",required = false,dataType = "int",paramType = "form")
    public ResultDTO index(@RequestParam(value = "limit", defaultValue = "12") int limit) {
        return this.index(1, limit);
    }

    /**
     * 首页分页
     *
     * @param p       第几页
     * @param limit   每页大小
     * @return 主页
     */
    @GetMapping(value = "page/{p}")
    @ApiOperation(value = "首页-文章分页",notes = "查询文章第p页，默认查询12条")
    @ApiImplicitParams({
            @ApiImplicitParam(name="p",required = false,dataType = "int",paramType = "query"),
            @ApiImplicitParam(name="limit",value = "每页条数", required = false,dataType = "int",paramType = "form")
    })
    public ResultDTO index(@PathVariable int p, @RequestParam(value = "limit", defaultValue = "12") int limit) {
        p = p < 0 || p > WebConst.MAX_PAGE ? 1 : p;
        PageInfo<ContentVo> articles = contentService.getContents(p, limit);
        ResultDTO result = new ResultDTO(articles);
        if (p > 1) {
            result.putAddInfos("title","第" + p + "页");
        }
        return result;
    }

    /**
     * 文章页
     *
     * @param cid     文章主键
     * @return
     */
    @GetMapping(value = {"article/{cid}"})
    @ApiOperation(value = "通过文件id查询文章详情",notes = "通过cid（文章id）查询文章详情,comments表示评论内容")
    @ApiImplicitParam(name="cid",required = true,dataType = "String",paramType = "query")
    public ResultDTO getArticle(@PathVariable String cid) {
        ContentVo contents = contentService.getContents(cid);
        ResultDTO result = new ResultDTO(contents);
        if (null == contents || "draft".equals(contents.getStatus())) {
            return result.setCode(ResultDTO.STATUS_CODE_BUSINESS_ERROR);
        }
        result.putAddInfos("is_post", true);
        completeArticle(result, contents);
        updateArticleHit(contents.getCid(), contents.getHits());
        return result;
    }

    /**
     * 文章页(预览)
     *
     * @param request 请求
     * @param cid     文章主键
     * @return
     */
    @GetMapping(value = {"article/{cid}/preview"})
    @ApiOperation(value = "通过文件id查询文章详情",notes = "通过cid（文章id）预览文章")
    @ApiImplicitParam(name="cid",value = "文章id",required = true,dataType = "String",paramType = "query")
    public ResultDTO articlePreview(@PathVariable String cid) {
        ContentVo contents = contentService.getContents(cid);
        ResultDTO result = new ResultDTO(contents);
        if (null == contents) {
            return result.setCode(ResultDTO.STATUS_CODE_BUSINESS_ERROR);
        }
        result.putAddInfos("is_post", true);
        completeArticle(result, contents);
        updateArticleHit(contents.getCid(), contents.getHits());
        return result;
    }

    /**
     * 抽取公共方法
     *
     * @param result
     * @param contents
     */
    private void completeArticle(ResultDTO result, ContentVo contents) {
        if (contents.getAllowComment()) {
            String cp = "1";
            result.putAddInfos("cp", cp);
            PageInfo<CommentBo> commentsPaginator = commentService.getComments(contents.getCid(), Integer.parseInt(cp), 6);
            result.putAddInfos("comments", commentsPaginator);
        }
    }

    /**
     * 更新文章的点击率
     *
     * @param cid
     * @param chits
     */
    @Transactional(rollbackFor = TipException.class)
    void updateArticleHit(Integer cid, Integer chits) {
        Integer hits = cache.hget("article", "hits");
        if (chits == null) {
            chits = 0;
        }
        hits = null == hits ? 1 : hits + 1;
        if (hits >= WebConst.HIT_EXCEED) {
            ContentVo temp = new ContentVo();
            temp.setCid(cid);
            temp.setHits(chits + hits);
            contentService.updateContentByCid(temp);
            cache.hset("article", "hits", 1);
        } else {
            cache.hset("article", "hits", hits);
        }
    }

    /**
     * 注销
     *
     * @param session
     * @param response
     */
    @PutMapping("logout")
    @ApiOperation(value = "注销？？",notes = "注销？？")
    public void logout(HttpSession session, HttpServletResponse response) {
        TaleUtils.logout(session, response);
    }

    /**
     * 评论操作
     */
    @PostMapping(value = "comment")
    @ApiOperation(value = "提交评论",notes = "提交评论")
    @Transactional(rollbackFor = TipException.class)
    public ResultDTO comment(@RequestParam Integer cid, @RequestParam Integer coid,
                                  @RequestParam String author, @RequestParam String mail,
                                  @RequestParam String url, @RequestParam String text, @RequestParam String _csrf_token) {
        HttpServletRequest request = WebUtils.getRequest();
        String ref = request.getHeader("Referer");
        if (StringUtils.isBlank(ref) || StringUtils.isBlank(_csrf_token)) {
            return ResultDTO.fail(ResultDTO.STATUS_CODE_REQUEST_WRONG,"");
        }

        String token = cache.hget(Types.CSRF_TOKEN.getType(), _csrf_token);
        if (StringUtils.isBlank(token)) {
            return ResultDTO.fail(ResultDTO.STATUS_CODE_REQUEST_WRONG,"");
        }

        if (null == cid || StringUtils.isBlank(text)) {
            return ResultDTO.fail(ResultDTO.STATUS_CODE_REQUEST_WRONG,"请输入完整后评论");
        }

        if (StringUtils.isNotBlank(author) && author.length() > 50) {
            return ResultDTO.fail(ResultDTO.STATUS_CODE_REQUEST_WRONG,"姓名过长");
        }

        if (StringUtils.isNotBlank(mail) && !TaleUtils.isEmail(mail)) {
            return ResultDTO.fail(ResultDTO.STATUS_CODE_REQUEST_WRONG,"请输入正确的邮箱格式");
        }

        if (StringUtils.isNotBlank(url) && !PatternKit.isURL(url)) {
            return ResultDTO.fail(ResultDTO.STATUS_CODE_REQUEST_WRONG,"请输入正确的URL格式");
        }

        if (text.length() > 200) {
            return ResultDTO.fail(ResultDTO.STATUS_CODE_REQUEST_WRONG,"请输入200个字符以内的评论");
        }

        String val = IPKit.getIpAddrByRequest(request) + ":" + cid;
        Integer count = cache.hget(Types.COMMENTS_FREQUENCY.getType(), val);
        if (null != count && count > 0) {
            return ResultDTO.fail(ResultDTO.STATUS_CODE_REQUEST_WRONG,"您发表评论太快了，请过会再试");
        }

        author = TaleUtils.cleanXSS(author);
        text = TaleUtils.cleanXSS(text);

        author = EmojiParser.parseToAliases(author);
        text = EmojiParser.parseToAliases(text);

        CommentVo comments = new CommentVo();
        comments.setAuthor(author);
        comments.setCid(cid);
        comments.setIp(request.getRemoteAddr());
        comments.setUrl(url);
        comments.setContent(text);
        comments.setMail(mail);
        comments.setParent(coid);
        try {
            commentService.insertComment(comments);
            cookie("tale_remember_author", URLEncoder.encode(author, "UTF-8"), 7 * 24 * 60 * 60, WebUtils.getResponse());
            cookie("tale_remember_mail", URLEncoder.encode(mail, "UTF-8"), 7 * 24 * 60 * 60, WebUtils.getResponse());
            if (StringUtils.isNotBlank(url)) {
                cookie("tale_remember_url", URLEncoder.encode(url, "UTF-8"), 7 * 24 * 60 * 60, WebUtils.getResponse());
            }
            // 设置对每个文章1分钟可以评论一次
            cache.hset(Types.COMMENTS_FREQUENCY.getType(), val, 1, 60);
            return ResultDTO.ok();
        } catch (Exception e) {
            String msg = "评论发布失败";
            if (e instanceof TipException) {
                msg = e.getMessage();
            } else {
                LOGGER.error(msg, e);
            }
            return ResultDTO.fail(msg);
        }
    }

    /**
     * 设置cookie
     *
     * @param name
     * @param value
     * @param maxAge
     * @param response
     */
    private void cookie(String name, String value, int maxAge, HttpServletResponse response) {
        Cookie cookie = new Cookie(name, value);
        cookie.setMaxAge(maxAge);
        cookie.setSecure(false);
        response.addCookie(cookie);
    }

    /**
     * 分类页
     *
     * @return
     */
    @GetMapping(value = "category/{keyword}")
    @ApiOperation(value = "通过分类查询文章列表",notes = "通过分类查询文章列表")
    public ResultDTO categories(@PathVariable String keyword, @RequestParam(value = "limit", defaultValue = "12") int limit) {
        return this.categories(keyword, 1, limit);
    }

    /**
     * 分类分页
     *
     * @return
     */
    @GetMapping(value = "category/{keyword}/{page}")
    @ApiOperation(value = "通过分类查询文章列表-带分页参数",notes = "通过分类查询文章列表-带分页参数")
    public ResultDTO categories(@PathVariable String keyword,
                                @PathVariable int page,
                                @RequestParam(value = "limit", defaultValue = "12") int limit) {
        page = page < 0 || page > WebConst.MAX_PAGE ? 1 : page;
        MetaDto metaDto = metaService.getMeta(Types.CATEGORY.getType(), keyword);
        if(metaDto == null){
            return ResultDTO.fail("没有分类信息");
        }

        PageInfo<ContentVo> contentsPaginator = contentService.getArticles(metaDto.getMid(), page, limit);
        ResultDTO result = new ResultDTO(contentsPaginator);
        result.putAddInfos("meta", metaDto);
        result.putAddInfos("type", "分类");
        result.putAddInfos("keyword", keyword);

        return result;
    }

    /**
     * 归档页
     *
     * @return
     */
    @GetMapping(value = "archives")
    @ApiOperation(value = "分月份查询文章列表",notes = "分月份查询文章列表")
    public ResultDTO archives() {
        return new ResultDTO(siteService.getArchives());
    }


    /**
     * 友链页
     *
     * @return
     */
    @GetMapping(value = "links")
    @ApiOperation(value = "查询友链",notes = "查询友链")
    public ResultDTO links(HttpServletRequest request) {
        return new ResultDTO(metaService.getMetas(Types.LINK.getType()));
    }

    /**
     * 自定义页面,如关于的页面
     */
    @GetMapping(value = "customPage/{pagename}")
    @ApiOperation(value = "自定义页面,如关于的页面????",notes = "自定义页面,如关于的页面?????")
    public ResultDTO page(@PathVariable String pagename) {
        ContentVo contents = contentService.getContents(pagename);
        if (null == contents) {
            return ResultDTO.fail();
        }
        HttpServletRequest request = WebUtils.getRequest();
        ResultDTO result = new ResultDTO();
        if (contents.getAllowComment()) {
            String cp = request.getParameter("cp");
            if (StringUtils.isBlank(cp)) {
                cp = "1";
            }
            PageInfo<CommentBo> commentsPaginator = commentService.getComments(contents.getCid(), Integer.parseInt(cp), 6);
            result.setData(commentsPaginator);
            request.setAttribute("comments", commentsPaginator);
        }
        result.putAddInfos("article", contents);
        updateArticleHit(contents.getCid(), contents.getHits());
        return result;
    }


    /**
     * 搜索页
     *
     * @param keyword
     * @return
     */
    @GetMapping(value = "search/{keyword}")
    @ApiOperation(value = "通过关键字搜索文章",notes = "通过关键字搜索文章")
    public ResultDTO search(@PathVariable String keyword, @RequestParam(value = "limit", defaultValue = "12") int limit) {
        return this.search(keyword, 1, limit);
    }

    @GetMapping(value = "search/{keyword}/{page}")
    @ApiOperation(value = "通过关键字搜索文章-带分页参数",notes = "通过关键字搜索文章-带分页参数")
    public ResultDTO search(@PathVariable String keyword, @PathVariable int page, @RequestParam(value = "limit", defaultValue = "12") int limit) {
        page = page < 0 || page > WebConst.MAX_PAGE ? 1 : page;
        PageInfo<ContentVo> articles = contentService.getArticles(keyword, page, limit);
        ResultDTO result = new ResultDTO(articles);
        result.putAddInfos("type", "搜索");
        result.putAddInfos("keyword", keyword);
        return result;
    }


    /**
     * 标签页
     *
     * @param name
     * @return
     */
    @GetMapping(value = "tag/{name}")
    @ApiOperation(value = "通过标签搜索文章",notes = "通过标签搜索文章")
    public ResultDTO tags(@PathVariable String name, @RequestParam(value = "limit", defaultValue = "12") int limit) {
        return this.tags(name, 1, limit);
    }

    /**
     * 标签分页
     *
     * @param name
     * @param page
     * @param limit
     * @return
     */
    @GetMapping(value = "tag/{name}/{page}")
    @ApiOperation(value = "通过标签搜索文章-带分页参数",notes = "通过标签搜索文章-带分页参数")
    public ResultDTO tags(@PathVariable String name, @PathVariable int page, @RequestParam(value = "limit", defaultValue = "12") int limit) {

        page = page < 0 || page > WebConst.MAX_PAGE ? 1 : page;
//        对于空格的特殊处理
        name = name.replaceAll("\\+", " ");
        MetaDto metaDto = metaService.getMeta(Types.TAG.getType(), name);
        if (null == metaDto) {
            return ResultDTO.fail();
        }

        ResultDTO result = new ResultDTO(contentService.getArticles(metaDto.getMid(), page, limit));
        result.putAddInfos("meta", metaDto);
        result.putAddInfos("type", "标签");
        result.putAddInfos("keyword", name);

        return result;
    }
}
