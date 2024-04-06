// const SERVER_URL = "http://localhost:8080"
const SERVER_URL = ""
const DEFAULT_CATEGORY_POST_ALL = "Articles"

const NUMBER_OF_PAGE_BTN_IN_A_PAGE = 10
const PAGE_SIZE = 13
const MOST_VIEWED_SIZE = 5
const RECENT_ARTICLE_SIZE = 5
const RECENT_COMMENT_SIZE = 5

let currentPage = 1
let currentCategory = DEFAULT_CATEGORY_POST_ALL

// 초기화
document.addEventListener("DOMContentLoaded", function () {
    updateAboutBar();
    updateViewCount()
    loadMostViewArticle()
    loadCategories()
    updateRecentPosts()
    updateRecentComments()
    loadPosts(DEFAULT_CATEGORY_POST_ALL, currentPage, PAGE_SIZE)
    updatePaginationBar();
});

// 소개 바를 그리는 함수
function updateAboutBar() {
    document.getElementById("ecsimsw-about").addEventListener('click', function () {
        window.location.href = 'https://github.com/ecsimsw/blog.me';
    })
    document.getElementById("ecsimsw-tistory").addEventListener('click', function () {
        window.location.href = 'https://ecsimsw.tistory.com/';
    })
    document.getElementById("ecsimsw-github").addEventListener('click', function () {
        window.location.href = 'https://github.com/ecsimsw';
    })
}

// 조회 수를 조회하여 그리는 함수
function updateViewCount() {
    const yesterdayDate = new Date(new Date().setDate(new Date().getDate() - 1))
    fetchData(SERVER_URL + "/api/view/daily?date=" + yesterdayDate.toISOString().substring(0, 10), function (count) {
        const yesterdayViewCnt = document.getElementById("yesterdayViewCnt");
        yesterdayViewCnt.textContent = "yesterday : " + count
    })
    fetchData(SERVER_URL + "/api/view/total", function (count) {
        const totalViewCnt = document.getElementById("totalViewCnt");
        totalViewCnt.textContent = "total : " + count
    })
}

// 최다 조회 수 게시물을 그리는 함수
function loadMostViewArticle() {
    fetchData(SERVER_URL + "/api/view/top/total?top=" + MOST_VIEWED_SIZE, function (posts) {
        const topArticle = document.getElementById("most-viewed");
        // topArticle.innerHTML = ""
        let i = 1
        posts.forEach(function (post) {
            const li = document.createElement("li")
            const a = document.createElement("a")
            li.id = "most-viewed-" + i++
            a.href = "/api/article/" + post.id
            a.textContent = post.title
            a.className = "most-viewed-item"
            a.textContent = post.title
            li.appendChild(a)
            topArticle.appendChild(li)
        })
    })
}

// 최근 글 목록을 업데이트 하는 함수
function updateRecentPosts() {
    fetchData(SERVER_URL + "/api/recent/article?n=" + RECENT_ARTICLE_SIZE, function (posts) {
        const recentArticles = document.getElementById("recent-posts");
        let i = 1
        posts.forEach(function (post) {
            const li = document.createElement("li")
            const a = document.createElement("a")
            li.id = "recent-posts-" + i++
            a.href = post.url
            a.textContent = post.title
            a.className = "recent-posts-item"
            a.textContent = post.title
            li.appendChild(a)
            recentArticles.appendChild(li)
        })
    })
}

// 최근 댓글 목록을 업데이트 하는 함수
function updateRecentComments() {
    fetchData(SERVER_URL + "/api/recent/comment?n=" + RECENT_COMMENT_SIZE, function (posts) {
        const recentArticles = document.getElementById("recent-comments");
        let i = 1
        posts.forEach(function (post) {
            const li = document.createElement("li")
            const a = document.createElement("a")
            li.id = "recent-comments-" + i++
            a.href = post.url
            a.textContent = post.comment
            a.className = "recent-comments-item"
            a.textContent = post.comment
            li.appendChild(a)
            recentArticles.appendChild(li)
        })
    })
}

// 카테고리 목록을 조회하여 그리는 함수
function loadCategories() {
    const categoryList = document.getElementById("category-list")
    fetchData(SERVER_URL + "/api/article/count", function (count) {
        addCategory(categoryList, DEFAULT_CATEGORY_POST_ALL, count)
        fetchData(SERVER_URL + "/api/category", function (categories) {
            categories.forEach(function (category) {
                addCategory(categoryList, category.name, category.numberOfPosts)
            });
        })
    })
}

// 카테고리 하위 내용을 추가하는 함수
function addCategory(categoryList, categoryName, numberOfPosts) {
    const li = document.createElement("li")
    const a = document.createElement("a")
    a.className = "category-name"
    a.textContent = categoryName + "(" + numberOfPosts + ")"
    a.addEventListener('click', function () {
        let contentTitle = document.getElementById("content-title")
        contentTitle.textContent = categoryName
        currentPage = 1
        currentCategory = categoryName
        loadPosts(categoryName, currentPage, PAGE_SIZE)
        renderPagination(numberOfPosts)
        document.getElementById('page-btn-' + currentPage)
            .classList.add('selected')
    })
    li.appendChild(a)
    categoryList.appendChild(li)
}

// category 에 해당하는 글을 조회하여 그리는 함수
function loadPosts(categoryName, pageNumber, pageSize) {
    var pageIndex = pageNumber - 1
    if (categoryName === DEFAULT_CATEGORY_POST_ALL) {
        fetchData(
            SERVER_URL + "/api/article" +
            "?pageNumber=" + pageIndex +
            "&pageSize=" + pageSize
            , renderPosts
        )
    } else {
        fetchData(
            SERVER_URL + "/api/article" +
            "?category=" + categoryName +
            "&pageNumber=" + pageIndex +
            "&pageSize=" + pageSize
            , renderPosts
        )
    }
}

// 게시글 목록을 그리는 함수
function renderPosts(posts) {
    const postList = document.getElementById("post-list")
    postList.innerHTML = ""
    posts.forEach(function (post) {
        const li = document.createElement("li")
        const a = document.createElement("a")
        const h3 = document.createElement("h3")
        const p = document.createElement("p")
        a.href = "/api/article/" + post.id
        a.className = "post-list-item"
        a.textContent = post.title
        h3.appendChild(a)
        li.appendChild(h3)
        li.appendChild(p)
        postList.appendChild(li)
    })
}

// 페이지네이션 버튼을 그리는 함수
function renderPagination(totalItems) {
    const totalPages = Math.ceil(totalItems / PAGE_SIZE);
    const startPageIndex = Math.min(currentPage, currentPage - (currentPage % NUMBER_OF_PAGE_BTN_IN_A_PAGE) + 1)
    const endPageIndex = Math.min(startPageIndex + NUMBER_OF_PAGE_BTN_IN_A_PAGE - 1, totalPages)
    const pagination = document.getElementById("pagination");
    pagination.innerHTML = "";
    loadPosts(currentCategory, currentPage, PAGE_SIZE)

    if (startPageIndex > NUMBER_OF_PAGE_BTN_IN_A_PAGE) {
        const prevBtn = document.createElement("li");
        prevBtn.textContent = "<";
        prevBtn.className = "page-btn";
        prevBtn.addEventListener("click", function () {
            currentPage = startPageIndex - NUMBER_OF_PAGE_BTN_IN_A_PAGE
            renderPagination(totalItems)
        });
        pagination.appendChild(prevBtn);
    }

    for (let i = startPageIndex; i <= endPageIndex; i++) {
        const button = document.createElement("li")
        button.textContent = i;
        button.className = "page-btn";
        button.id = "page-btn-" + i;
        button.addEventListener("click", function () {
            if (document.getElementsByClassName("selected").length > 0) {
                document.getElementsByClassName("selected")[0]
                    .classList.remove("selected")
            }
            currentPage = i
            document.getElementById('page-btn-' + currentPage)
                .classList.add('selected')
            loadPosts(currentCategory, i, PAGE_SIZE)
        });
        pagination.appendChild(button);
    }

    if (endPageIndex < totalPages) {
        const nextBtn = document.createElement("li");
        nextBtn.textContent = ">";
        nextBtn.className = "page-btn";
        nextBtn.addEventListener("click", function () {
            currentPage = startPageIndex + NUMBER_OF_PAGE_BTN_IN_A_PAGE
            renderPagination(totalItems)
        });
        pagination.appendChild(nextBtn);
    }

    if (document.getElementsByClassName("selected").length > 0) {
        document.getElementsByClassName("selected")[0]
            .classList.remove("selected")
        document.getElementById('page-btn-' + currentPage)
            .classList.add('selected')
    }
}

// 선택된 카테고리의 글 개수만큼 페이지네이션 바를 그리는 함수
function updatePaginationBar() {
    fetchData(SERVER_URL + "/api/article/count", function (count) {
        renderPagination(count)
        document.getElementById('page-btn-' + currentPage)
            .classList.add('selected')
    })
}


// 서버에서 데이터를 받아오는 함수
function fetchData(url, callback) {
    fetch(url)
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        })
        .then(data => callback(data))
        .catch(error => {
            console.log(error)
        });
}