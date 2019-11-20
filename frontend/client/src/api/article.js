import Axios from 'axios';
// 为了让服务端渲染正确请求数据
if (typeof window === 'undefined') {
    Axios.defaults.baseURL = 'http://127.0.0.1:8088/';
}
export default {
    getAllPublishArticles(tag = '', page = 1, limit = 0) {
        return Axios.get(`/api/publishArticles?tag=${tag}&page=${page}&limit=${limit}`);
    },
    getArticle(id) {
        return Axios.get('/api/articles/' + id);
    },
};
