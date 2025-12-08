'use client';

import { useEffect, useState, ChangeEvent, FormEvent } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { productAPI, cartAPI, reviewAPI } from '@/lib/api';
import { isAuthenticated } from '@/lib/auth';

interface Product {
  id: number;
  product_name: string;
  price: number;
  quantity: number;
  categoryName?: string;
}

interface Review {
  userName: string;
  productId: number;
  rate: number;   
  comment: string;
}



export default function ProductDetailPage() {
  const params = useParams();
  const router = useRouter();
  const [product, setProduct] = useState<Product | null>(null);
  const [reviews, setReviews] = useState<Review[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [quantity, setQuantity] = useState<number>(1);
  const [reviewText, setReviewText] = useState<string>('');
  const [rating, setRating] = useState<number>(5);

  useEffect(() => {
    fetchProduct();
    fetchReviews();
  }, [params.id]);

  const fetchProduct = async (): Promise<void> => {
    try {
      const data = await productAPI.getById(Number(params.id));
      setProduct(data);
    } catch (error) {
      console.error('Error fetching product:', error);
    } finally {
      setLoading(false);
    }
  };

  const fetchReviews = async (): Promise<void> => {
    try {
      const data = await reviewAPI.getByProduct(Number(params.id));
      setReviews(data);
    } catch (error) {
      console.error('Error fetching reviews:', error);
    }
  };

  const handleAddToCart = async (): Promise<void> => {
    if (!isAuthenticated()) {
      router.push('/login');
      return;
    }

    try {
      await cartAPI.updateItem(Number(params.id), quantity);
      alert('Đã thêm vào giỏ hàng!');
    } catch (error) {
      console.error('Error adding to cart:', error);
      alert('Có lỗi xảy ra. Vui lòng thử lại.');
    }
  };

  const handleSubmitReview = async (e: FormEvent<HTMLFormElement>): Promise<void> => {
    e.preventDefault();
    if (!isAuthenticated()) {
      router.push('/login');
      return;
    }

    try {
      await reviewAPI.create(Number(params.id), {
        comment: reviewText,
        rating: rating,
      });
      setReviewText('');
      setRating(5);
      fetchReviews();
      alert('Đã gửi đánh giá!');
    } catch (error) {
      console.error('Error submitting review:', error);
      alert('Có lỗi xảy ra. Vui lòng thử lại.');
    }
  };

  const formatPrice = (price: number): string => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
    }).format(price);
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-screen">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  if (!product) {
    return (
      <div className="text-center py-12">
        <p className="text-gray-500 text-lg">Không tìm thấy sản phẩm</p>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto space-y-12">
      <div className="grid md:grid-cols-2 gap-8">
        <div className="bg-gray-100 rounded-lg overflow-hidden">
          <img
            src={productAPI.getImage(product.id)}
            alt={product.product_name}
            className="w-full h-full object-contain"
            onError={(e: React.SyntheticEvent<HTMLImageElement, Event>) => {
              e.currentTarget.src = '/placeholder-product.png';
            }}
          />
        </div>

        <div className="space-y-6">
          <div>
            <h1 className="text-3xl font-bold text-gray-800 mb-2">{product.product_name}</h1>
            {product.categoryName && (
              <span className="inline-block px-3 py-1 bg-blue-100 text-blue-700 text-sm rounded-full">
                {product.categoryName}
              </span>
            )}
          </div>

          <div className="text-4xl font-bold text-blue-600">
            {formatPrice(product.price)}
          </div>

          <div className="flex items-center space-x-4">
            <span className="text-gray-700 font-medium">Còn lại:</span>
            <span className={`font-bold ${product.quantity > 0 ? 'text-green-600' : 'text-red-600'}`}>
              {product.quantity} sản phẩm
            </span>
          </div>

          {product.quantity > 0 && (
            <div className="space-y-4">
              <div className="flex items-center space-x-4">
                <label className="text-gray-700 font-medium">Số lượng:</label>
                <div className="flex items-center border rounded-lg">
                  <button
                    onClick={() => setQuantity(Math.max(1, quantity - 1))}
                    className="px-4 py-2 hover:bg-gray-100"
                  >
                    -
                  </button>
                  <input
                    type="number"
                    value={quantity}
                    onChange={(e: ChangeEvent<HTMLInputElement>) => setQuantity(Math.max(1, parseInt(e.target.value) || 1))}
                    className="w-16 text-center border-x py-2"
                    min="1"
                    max={product.quantity}
                  />
                  <button
                    onClick={() => setQuantity(Math.min(product.quantity, quantity + 1))}
                    className="px-4 py-2 hover:bg-gray-100"
                  >
                    +
                  </button>
                </div>
              </div>

              <button
                onClick={handleAddToCart}
                className="w-full py-3 bg-blue-600 text-white rounded-lg font-semibold hover:bg-blue-700 transition"
              >
                Thêm vào giỏ hàng
              </button>
            </div>
          )}
        </div>
      </div>

      <div className="space-y-6">
        <h2 className="text-2xl font-bold text-gray-800">Đánh giá sản phẩm</h2>

        {isAuthenticated() && (
          <form onSubmit={handleSubmitReview} className="bg-white p-6 rounded-lg shadow-md space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Đánh giá của bạn
              </label>
              <div className="flex space-x-2">
                {[1, 2, 3, 4, 5].map((star) => (
                  <button
                    key={star}
                    type="button"
                    onClick={() => setRating(star)}
                    className={`text-2xl ${star <= rating ? 'text-yellow-400' : 'text-gray-300'}`}
                  >
                    ★
                  </button>
                ))}
              </div>
            </div>

            <div>
              <textarea
                value={reviewText}
                onChange={(e: ChangeEvent<HTMLTextAreaElement>) => setReviewText(e.target.value)}
                required
                rows={4}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                placeholder="Chia sẻ trải nghiệm của bạn về sản phẩm..."
              />
            </div>

            <button
              type="submit"
              className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition"
            >
              Gửi đánh giá
            </button>
          </form>
        )}

        <div className="space-y-4">
          {reviews.length > 0 ? (
            reviews.map((review) => (
              <div key={review.userName} className="bg-white p-6 rounded-lg shadow-md">
                <div className="flex items-center justify-between mb-2">
                  <span className="font-semibold text-gray-800">{review.userName}</span>
                  <div className="flex">
                    {[...Array(5)].map((_, i) => (
                      <span
                        key={i}
                        className={`text-lg ${i < review.rate ? 'text-yellow-400' : 'text-gray-300'}`}
                      >
                        ★
                      </span>
                    ))}
                  </div>
                </div>
                <p className="text-gray-700">{review.comment}</p>
              </div>
            ))
          ) : (
            <p className="text-center text-gray-500 py-8">Chưa có đánh giá nào</p>
          )}
        </div>
      </div>
    </div>
  );
}