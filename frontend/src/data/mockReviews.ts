import { Review } from "../types";

export const mockReviews: Review[] = [
  {
    id: "review-1",
    serviceId: "1",
    reviewerId: "user-4",
    reviewerName: "Ahmet Can",
    reviewerAvatar: "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=100&h=100&fit=crop",
    rating: 5,
    comment: "Selin's meditation sessions are incredibly peaceful and rejuvenating. The location by the Bosphorus adds to the experience. Highly recommend!",
    createdAt: "2025-10-20",
  },
  {
    id: "review-2",
    serviceId: "1",
    reviewerId: "user-5",
    reviewerName: "Zeynep Arslan",
    reviewerAvatar: "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=100&h=100&fit=crop",
    rating: 5,
    comment: "Perfect way to start the weekend. Selin is a wonderful instructor who makes everyone feel welcome.",
    createdAt: "2025-10-18",
  },
  {
    id: "review-3",
    serviceId: "1",
    reviewerId: "user-9",
    reviewerName: "Mert Acar",
    reviewerAvatar: "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=100&h=100&fit=crop",
    rating: 5,
    comment: "Great for beginners like me. The setting is beautiful and Selin is patient and encouraging.",
    createdAt: "2025-10-15",
  },
  {
    id: "review-4",
    serviceId: "8",
    reviewerId: "user-2",
    reviewerName: "Cem Öztürk",
    reviewerAvatar: "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=100&h=100&fit=crop",
    rating: 5,
    comment: "Fadime Teyze is a treasure! Learned to make amazing menemen and börek. The recipes have been in her family for generations.",
    createdAt: "2025-10-22",
  },
  {
    id: "review-5",
    serviceId: "8",
    reviewerId: "user-6",
    reviewerName: "Ali Rıza",
    reviewerAvatar: "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=100&h=100&fit=crop",
    rating: 5,
    comment: "Not just cooking lessons but also stories and wisdom. Fadime Teyze makes you feel like family!",
    createdAt: "2025-10-19",
  },
  {
    id: "review-6",
    serviceId: "8",
    reviewerId: "user-3",
    reviewerName: "Melisa Demir",
    reviewerAvatar: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100&h=100&fit=crop",
    rating: 5,
    comment: "The best home-cooked meal I've had in years. Learned so much and made a new friend.",
    createdAt: "2025-10-16",
  },
  {
    id: "review-7",
    serviceId: "3",
    reviewerId: "user-1",
    reviewerName: "Selin Karaca",
    reviewerAvatar: "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=100&h=100&fit=crop",
    rating: 5,
    comment: "Melisa has an amazing eye for composition. Learned so many tips and got some great shots!",
    createdAt: "2025-10-24",
  },
  {
    id: "review-8",
    serviceId: "7",
    reviewerId: "user-current",
    reviewerName: "You",
    reviewerAvatar: "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=100&h=100&fit=crop",
    rating: 5,
    comment: "Ece's yoga sessions are exactly what I needed. The sunset view is magical!",
    createdAt: "2025-10-21",
  },
];

// Get reviews for a specific service
export const getReviewsByServiceId = (serviceId: string): Review[] => {
  return mockReviews.filter((review) => review.serviceId === serviceId);
};

// Calculate average rating for a service
export const getAverageRating = (serviceId: string): number => {
  const reviews = getReviewsByServiceId(serviceId);
  if (reviews.length === 0) return 0;
  const sum = reviews.reduce((acc, review) => acc + review.rating, 0);
  return sum / reviews.length;
};

// Get review count for a service
export const getReviewCount = (serviceId: string): number => {
  return getReviewsByServiceId(serviceId).length;
};

