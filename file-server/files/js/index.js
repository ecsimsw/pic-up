const currentDomain = window.location.origin

const tableBody = document.getElementById('table-body');
const pagination = document.getElementById('pagination');

const productNameInput = document.getElementById('productName');
const pageSizeSelection = document.getElementById('pageSize');
const minPriceSelection = document.getElementById('minPrice');
const maxPriceSelection = document.getElementById('maxPrice');
const sortBySelection = document.getElementById('sortBy');

const meButton = document.getElementById('me');

let pageSize = 10
let pageCount = 5
let startPage = 1;
let currentPage = 1;
let endPage = startPage + pageCount -1;
let startProductRead;
let lastProductRead;
let products = []

function orderButtonEventHandler(productId) {
    const orderUrl = currentDomain + "/api/products/order"
    const data = {
        productId: productId,
        userId: 1,
        quantity: 1
    }
    axios({
        method: "post",
        url: currentDomain + orderUrl,
        params: data
    }).then(() => {
        window.location.reload();
    }).catch(function () {
        if (confirm("Error")) {
            window.location.reload();
        }
    })
}

productNameInput.addEventListener("keydown", function(event) {
    if(event.keyCode === 13) {
        fetchAndDisplayProducts().then(() => {});
    }
});

productNameInput.addEventListener("change", function(event) {
    fetchAndDisplayProducts().then(() => {});
});

minPriceSelection.addEventListener("change", (event) => {
    if(maxPriceSelection.value !== '' && minPriceSelection.value > maxPriceSelection.value) {
        alert("invalid price search range")
        minPriceSelection.value = maxPriceSelection.value
        return
    }
    fetchAndDisplayProducts().then(() => {});
});

maxPriceSelection.addEventListener("change", (event) => {
    if(minPriceSelection.value !== '' && minPriceSelection.value > maxPriceSelection.value) {
        alert("invalid price search range")
        maxPriceSelection.value = minPriceSelection.value
        return
    }
    fetchAndDisplayProducts().then(() => {});
});

sortBySelection.addEventListener("change", (event) => {
  fetchAndDisplayProducts().then(() => {});
});

pageSizeSelection.addEventListener("change", (event) => {
  fetchAndDisplayProducts().then(() => {});
});

me.addEventListener("click", (event) => {
    loginButton().then(()=>{});
})

async function loginButton() {
    const response = await fetch( "http://localhost:8082" + "/api/auth/me", {
        method: "GET",
        headers: {
            "Content-Type": "application/json",
        },
        credentials: 'include'
    });

    console.log(response)
    if(response.status == 401) {
      alert("unathorized")
      location.href = '../html/sign.html';
    } else if (!response.ok) {
        throw new Error('Error fetching products.');
    }
    const responseJson = await response.json();
    const memberId = responseJson.id;
    const memberName = responseJson.name;
    meButton.innerText = memberName
}

loginButton();

// Function to fetch and display products based on the current page and search query
async function fetchAndDisplayProducts() {
    pageSize = pageSizeSelection.options[pageSizeSelection.selectedIndex].value;
    startPage = 1;
    currentPage = 1;
    endPage = startPage + pageCount -1;
    startProductRead = undefined;
    lastProductRead = undefined
    products = await fetchProducts();
    startProductRead = products[0];
    lastProductRead = products[products.length-1]
    endPage = startPage + products.length / pageSize -1
    displayProducts()
    updatePagination()
}

async function fetchProducts() {
    try {
        const containsName = productNameInput.value.trim();
        const minPrice = minPriceSelection.value.trim();
        const maxPrice = maxPriceSelection.value.trim();
        const sortBy = sortBySelection.options[sortBySelection.selectedIndex].value;
        const pageSize = pageSizeSelection.options[pageSizeSelection.selectedIndex].value;
        const fetchSize = pageSize * pageCount;

        const fetchUrl = currentDomain + "/api/products/cursor/?"
        let response = await fetch(fetchUrl + new URLSearchParams({
            containsName: containsName,
            minPrice: minPrice,
            maxPrice: maxPrice,
            pageSize: fetchSize,
            productSortType : sortBy,
            sortBy: sortBy
        }));
        if (!response.ok) {
            throw new Error('Error fetching products.');
        }
        return await response.json();
    } catch (error) {
        console.log(error)
        alert('Error fetching products.')
        return [];
    }
}

async function fetchNextProducts() {
    try {
        const containsName = productNameInput.value.trim();
        const minPrice = minPriceSelection.value.trim();
        const maxPrice = maxPriceSelection.value.trim();
        const sortBy = sortBySelection.options[sortBySelection.selectedIndex].value;
        const fetchSize = pageSize * pageCount;

        const fetchUrl = currentDomain + "/api/products/cursor/next?"
        let response = await fetch(fetchUrl + new URLSearchParams({
            containsName: containsName,
            minPrice: minPrice,
            maxPrice: maxPrice,
            pageSize: fetchSize,
            cursorProductId: lastProductRead.id,
            cursorProductName: lastProductRead.name,
            cursorProductPrice: lastProductRead.price,
            productSortType : sortBy,
            sortBy: sortBy
        }));
        if (!response.ok) {
            throw new Error('Error fetching products.');
        }
        return await response.json();
    } catch (error) {
        console.log(error)
        alert('Error fetching products.')
        return [];
    }
}

async function fetchPrevProducts() {
    try {
        const containsName = productNameInput.value.trim();
        const minPrice = minPriceSelection.value.trim();
        const maxPrice = maxPriceSelection.value.trim();
        const sortBy = sortBySelection.options[sortBySelection.selectedIndex].value;
        const fetchSize = pageSize * pageCount;

        const fetchUrl = currentDomain + "/api/products/cursor/prev?"
        let response = await fetch(fetchUrl + new URLSearchParams({
            containsName: containsName,
            minPrice: minPrice,
            maxPrice: maxPrice,
            pageSize: fetchSize,
            cursorProductId: startProductRead.id,
            cursorProductName: startProductRead.name,
            cursorProductPrice: startProductRead.price,
            cursorProductQuantity: startProductRead.quantity,
            productSortType : sortBy,
            sortBy: sortBy
        }));
        if (!response.ok) {
            throw new Error('Error fetching products.');
        }
        return await response.json();
    } catch (error) {
        console.log(error)
        alert('Error fetching products.')
        return [];
    }
}

// Function to display products in the table
async function displayProducts() {
    tableBody.innerHTML = '';
    for(let index = 0; index < pageSize; index++) {
        const productIndex = ((currentPage -1) % pageCount ) * pageSize + index
        const product = products[productIndex]
        const row = document.createElement('tr');
        row.id = `product-${product.id}`
        row.innerHTML = `
          <td>${product.id}</td>
          <td>${product.name}</td>
          <td>${product.price}</td>
          <td>${product.quantity}</td>
          <td><button onclick="orderButtonEventHandler(${product.id})">Order</button></td>`;
        tableBody.appendChild(row);
    }
}

async function updatePagination() {
    pagination.innerHTML = '';
    if(startPage >= pageCount) {
        const leftArrow = document.createElement('a');
        leftArrow.href = '#';
        leftArrow.textContent = '<<';
        leftArrow.className = "pagination-link";
        leftArrow.addEventListener('click', async () => {
            currentPage = startPage - 1;
            endPage = currentPage
            startPage = startPage - pageCount
            products = await fetchPrevProducts();
            startProductRead = products[0];
            lastProductRead = products[products.length-1]
            await displayProducts();
            await updatePagination()
        });
        pagination.appendChild(leftArrow);
    }

    for (let pageNumber = startPage; pageNumber <= endPage; pageNumber++) {
        const link = document.createElement('a');
        link.href = '#';
        link.textContent = pageNumber;
        link.className = "pagination-link";
        if (pageNumber === currentPage) {
            link.classList.add('pagination-active');
        }
        link.addEventListener('click', async () => {
            currentPage = pageNumber;
            await displayProducts();
            await updatePagination()
        });
        pagination.appendChild(link);
    }

    if(products.length >= pageSize * pageCount) {
        const rightArrow = document.createElement('a');
        rightArrow.href = '#';
        rightArrow.textContent = '>>';
        rightArrow.className = "pagination-link";
        rightArrow.addEventListener('click', async () => {
            currentPage = endPage + 1;
            startPage = currentPage
            products = await fetchNextProducts()
            startProductRead = products[0];
            lastProductRead = products[products.length-1]
            endPage = startPage + products.length / pageSize -1
            await displayProducts()
            await updatePagination()
        });
        pagination.appendChild(rightArrow);
    }
}

// Initial fetch and display of products
fetchAndDisplayProducts();
