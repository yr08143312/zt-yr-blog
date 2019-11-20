import axios from 'axios';
export async function getAllPublishArticles(ctx) {
    const tag = ctx.query.tag;
    const page = +ctx.query.page;
    const limit = +ctx.query.limit || 4;
    let skip = 0;
    let articleArr;
    let allPage;
    let allNum;

    if (page !== 0) {
        skip = limit * (page - 1);
    }

    if (tag === '') {
        let response = await axios.get('http://localhost:8088/fuckBlog/')
        articleArr = response.data.data.list;
        allNum = 15;
    } else {
        let response = await axios.get('http://localhost:8088/fuckBlog/')
        articleArr = response.data.data.list;
        allNum = 15
    }

    allPage = Math.ceil(allNum / limit);


    ctx.body = {
        success: true,
        articleArr,
        allPage: allPage,
    };
}


export async function getArticle(ctx) {
    const id = ctx.params.id;
    if (id === '') {
        ctx.throw(400, 'id不能为空');
    }

    let response = await axios.get(`http://localhost:8088/fuckBlog/article/${id}`);
    const article = response.data.data;
    ctx.body = {
        success: true,
        article: article,
    };
}
