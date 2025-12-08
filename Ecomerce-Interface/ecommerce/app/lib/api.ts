const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api';

const getAuthHeader = (): Record<string, string> => {
  if (typeof window !== 'undefined') {
    const token = localStorage.getItem('accessToken');
    return token ? { Authorization: `Bearer ${token}` } : {};
  }
  return {};
};

// User API
export const userAPI = {
  register: async (userData: any) => {
    const res = await fetch(`${API_BASE_URL}/user/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(userData),
    });
    return res.json();
  },

  login: async (credentials: any) => {
    const res = await fetch(`${API_BASE_URL}/user/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(credentials),
    });
    return res.json();
  },

  logout: async () => {
    const res = await fetch(`${API_BASE_URL}/user/auth/logout`, {
      method: 'POST',
      headers: getAuthHeader(),
    });
    return res.json();
  },

  getProfile: async () => {
    const res = await fetch(`${API_BASE_URL}/user/profile`, {
      headers: getAuthHeader(),
    });
    return res.json();
  },

  updateProfile: async (id: number, userData: any) => {
    const res = await fetch(`${API_BASE_URL}/user/updation/${id}`, {
      method: 'PUT',
      headers: {
        ...getAuthHeader(),
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(userData),
    });
    return res.json();
  },

  refreshToken: async () => {
    if (typeof window !== 'undefined') {
      const refreshToken = localStorage.getItem('refreshToken');
      const res = await fetch(`${API_BASE_URL}/user/refresh`, {
        headers: { Authorization: `Bearer ${refreshToken}` },
      });
      return res.json();
    }
    return null;
  },
};

// Product API
export const productAPI = {
  getAll: async (noPage = 0, sizePage = 30) => {
    const res = await fetch(`${API_BASE_URL}/products/?noPage=${noPage}&sizePage=${sizePage}`);
    return res.json();
  },

  getById: async (id: number) => {
    const res = await fetch(`${API_BASE_URL}/products/${id}`);
    return res.json();
  },

  getImage: (id: number) => {
    return `${API_BASE_URL}/products/${id}/img`;
  },

  searchByName: async (name: string, noPage = 0, sizePage = 30) => {
    const res = await fetch(`${API_BASE_URL}/products/products?name=${name}&noPage=${noPage}&sizePage=${sizePage}`);
    return res.json();
  },
};

// Cart API
export const cartAPI = {
  getItems: async () => {
    const res = await fetch(`${API_BASE_URL}/cart/items`, {
      headers: getAuthHeader(),
    });
    return res.json();
  },

  updateItem: async (productId: number, quantity: number) => {
    const res = await fetch(`${API_BASE_URL}/cart/items/${productId}?quantity=${quantity}`, {
      method: 'PUT',
      headers: getAuthHeader(),
    });
    return res.json();
  },

  removeItem: async (productId: number) => {
    const res = await fetch(`${API_BASE_URL}/cart/items/${productId}`, {
      method: 'DELETE',
      headers: getAuthHeader(),
    });
    return res.json();
  },
};

// Order API
export const orderAPI = {
  create: async (paymentMethodId: number) => {
    const res = await fetch(`${API_BASE_URL}/orders?paymentMethodId=${paymentMethodId}`, {
      method: 'POST',
      headers: getAuthHeader(),
    });
    return res.json();
  },

  delete: async (orderId: number) => {
    const res = await fetch(`${API_BASE_URL}/orders/${orderId}`, {
      method: 'DELETE',
      headers: getAuthHeader(),
    });
    return res.json();
  },
};

// Review API
export const reviewAPI = {
  create: async (productId: number, reviewData: any) => {
    const res = await fetch(`${API_BASE_URL}/reviews/products/${productId}`, {
      method: 'POST',
      headers: {
        ...getAuthHeader(),
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(reviewData),
    });
    return res.json();
  },

  delete: async (productId: number, reviewId: number) => {
    const res = await fetch(`${API_BASE_URL}/reviews/products/${productId}/${reviewId}`, {
      method: 'DELETE',
      headers: getAuthHeader(),
    });
    return res.ok;
  },

  getByProduct: async (productId: number) => {
    const res = await fetch(`${API_BASE_URL}/reviews/products/${productId}`);
    return res.json();
  },
};

// Payment API
export const paymentAPI = {
  createVNPayUrl: async (totalPrice: number, orderInfo: string, orderId: number) => {
    const res = await fetch(`${API_BASE_URL}/payments/createUrl?totalPrice=${totalPrice}&orderInfo=${orderInfo}&orderId=${orderId}`, {
      method: 'POST',
    });
    return res.text();
  },

  addPaymentMethod: async (name: string) => {
    const res = await fetch(`${API_BASE_URL}/payments/methods?name=${name}`, {
      method: 'POST',
      headers: getAuthHeader(),
    });
    return res.json();
  },
};

// Invoice API
export const invoiceAPI = {
  create: async (orderId: number, paymentMethodId: number) => {
    const res = await fetch(`${API_BASE_URL}/invoices?orderId=${orderId}&paymentMethodId=${paymentMethodId}`, {
      method: 'POST',
      headers: getAuthHeader(),
    });
    return res.json();
  },

  delete: async (invoiceId: number) => {
    const res = await fetch(`${API_BASE_URL}/invoices/${invoiceId}`, {
      method: 'DELETE',
      headers: getAuthHeader(),
    });
    return res.ok;
  },
};

// Admin API
export const adminAPI = {
  getAllUsers: async (noPage = 0, sizePage = 20) => {
    const res = await fetch(`${API_BASE_URL}/admin/users?noPage=${noPage}&sizePage=${sizePage}`, {
      headers: getAuthHeader(),
    });
    return res.json();
  },

  getUser: async (id: number) => {
    const res = await fetch(`${API_BASE_URL}/admin/users/${id}`, {
      headers: getAuthHeader(),
    });
    return res.json();
  },

  deleteUser: async (id: number) => {
    const res = await fetch(`${API_BASE_URL}/admin/users/${id}`, {
      method: 'DELETE',
      headers: getAuthHeader(),
    });
    return res.json();
  },

  createProduct: async (productData: any, file ?: File) => {
    const formData = new FormData();
    formData.append('products', new Blob([JSON.stringify(productData)], { type: 'application/json' }));
    if (file) {
      formData.append('file', file);
    }
    if (typeof window !== 'undefined') {
      const token = localStorage.getItem('accessToken');
      const res = await fetch(`${API_BASE_URL}/admin/products`, {
        method: 'POST',
        headers: { Authorization: `Bearer ${token}` },
        body: formData,
      });
      return res.json();
    }
    return null;
  },

  updateProduct: async (id: number, productData: any, file?: File) => {
    const formData = new FormData();
    formData.append('products', new Blob([JSON.stringify(productData)], { type: 'application/json' }));
    if (file) formData.append('file', file);

    if (typeof window !== 'undefined') {
      const token = localStorage.getItem('accessToken');
      const res = await fetch(`${API_BASE_URL}/admin/products/${id}`, {
        method: 'PUT',
        headers: { Authorization: `Bearer ${token}` },
        body: formData,
      });
      return res.json();
    }
    return null;
  },

  deleteProduct: async (id: number) => {
    const res = await fetch(`${API_BASE_URL}/admin/products/${id}`, {
      method: 'DELETE',
      headers: getAuthHeader(),
    });
    return res.json();
  },
};