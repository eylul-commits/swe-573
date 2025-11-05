export interface ForumThread {
  id: string;
  title: string;
  author: {
    name: string;
    avatar: string;
    badge: string;
  };
  category: string;
  isPinned: boolean;
  views: number;
  replies: number;
  likes: number;
  lastActivity: string;
  excerpt: string;
}

export interface ForumCategory {
  name: string;
  count: number;
}

export const forumThreads: ForumThread[] = [
  {
    id: "1",
    title: "Best practices for first-time service exchange?",
    author: {
      name: "Ayşe Yılmaz",
      avatar: "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=100&h=100&fit=crop",
      badge: "⭐",
    },
    category: "Getting Started",
    isPinned: true,
    views: 234,
    replies: 18,
    likes: 42,
    lastActivity: "2 hours ago",
    excerpt: "I'm new to The Hive and wondering what are some good practices when doing my first service exchange. Any tips?",
  },
  {
    id: "2",
    title: "Community Meetup: Beşiktaş Square - Saturday 2pm",
    author: {
      name: "Mehmet Demir",
      avatar: "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=100&h=100&fit=crop",
      badge: "🏆",
    },
    category: "Events",
    isPinned: true,
    views: 456,
    replies: 32,
    likes: 78,
    lastActivity: "45 minutes ago",
    excerpt: "Join us for our monthly community gathering! Share experiences, meet neighbors, and learn about new services.",
  },
  {
    id: "3",
    title: "Success Story: Found a cooking mentor through The Hive!",
    author: {
      name: "Zeynep Kara",
      avatar: "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=100&h=100&fit=crop",
      badge: "⭐",
    },
    category: "Success Stories",
    isPinned: false,
    views: 189,
    replies: 24,
    likes: 65,
    lastActivity: "3 hours ago",
    excerpt: "Just wanted to share my amazing experience! Connected with Selin for cooking lessons and learned so much...",
  },
  {
    id: "4",
    title: "How do you manage your time bank balance?",
    author: {
      name: "Can Özdemir",
      avatar: "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=100&h=100&fit=crop",
      badge: "⚖️",
    },
    category: "Discussion",
    isPinned: false,
    views: 312,
    replies: 45,
    likes: 38,
    lastActivity: "5 hours ago",
    excerpt: "Curious how others keep their balance close to zero. Do you actively seek services when you have surplus hours?",
  },
  {
    id: "5",
    title: "Proposal: Add skill verification system",
    author: {
      name: "Elif Yıldız",
      avatar: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100&h=100&fit=crop",
      badge: "⭐",
    },
    category: "Suggestions",
    isPinned: false,
    views: 278,
    replies: 52,
    likes: 91,
    lastActivity: "1 day ago",
    excerpt: "What if we could verify certain skills through peer endorsements? This could help build more trust...",
  },
  {
    id: "6",
    title: "Tips for offering tech support to elderly neighbors",
    author: {
      name: "Burak Özkan",
      avatar: "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=100&h=100&fit=crop",
      badge: "🏆",
    },
    category: "Tips & Tricks",
    isPinned: false,
    views: 167,
    replies: 15,
    likes: 29,
    lastActivity: "1 day ago",
    excerpt: "I've been helping elderly community members with technology. Here are some things I've learned...",
  },
  {
    id: "7",
    title: "Questions about service ratings",
    author: {
      name: "Deniz Arslan",
      avatar: "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=100&h=100&fit=crop",
      badge: "🌱",
    },
    category: "Questions",
    isPinned: false,
    views: 145,
    replies: 12,
    likes: 8,
    lastActivity: "2 days ago",
    excerpt: "How do the 5-star ratings work exactly? Can someone explain the criteria?",
  },
];

export const categories: ForumCategory[] = [
  { name: "All", count: 47 },
  { name: "Getting Started", count: 8 },
  { name: "Events", count: 5 },
  { name: "Success Stories", count: 12 },
  { name: "Discussion", count: 15 },
  { name: "Suggestions", count: 4 },
  { name: "Tips & Tricks", count: 3 },
];

export const filterThreads = (
  threads: ForumThread[],
  searchQuery: string,
  selectedCategory: string
): ForumThread[] => {
  return threads.filter((thread) => {
    const matchesSearch =
      searchQuery === "" ||
      thread.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
      thread.excerpt.toLowerCase().includes(searchQuery.toLowerCase());

    const matchesCategory =
      selectedCategory === "All" || thread.category === selectedCategory;

    return matchesSearch && matchesCategory;
  });
};

