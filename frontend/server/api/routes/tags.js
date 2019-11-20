import * as $ from '../../controllers/tags_controller.js';

export default async(router) => {
    router.get('/tags', $.getAllTags)
};
