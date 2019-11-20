import * as $ from '../../controllers/articles_controller.js';
import verify from '../../middleware/verify.js';


export default async(router) => {
    router.get('/articles/:id', $.getArticle)
        .get('/publishArticles', $.getAllPublishArticles);
};
