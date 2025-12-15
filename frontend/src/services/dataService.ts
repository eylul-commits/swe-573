import {
  Service,
  ServiceFilters,
  ServiceRatingsResponse,
  ServiceQuestion,
} from "../types";
import {
  fetchActiveServices,
  fetchServiceById as fetchServiceByIdAPI,
  fetchAllTags,
  fetchServiceRatings,
  fetchServiceQuestions,
  createServiceQuestion,
  createQuestionAnswer,
} from "./apiService";

// Cache for services to avoid repeated API calls
let servicesCache: Service[] | null = null;
let cacheTimestamp: number | null = null;
const CACHE_DURATION = 60000; // 1 minute

// Helper to check if cache is valid
function isCacheValid(): boolean {
  return servicesCache !== null && 
         cacheTimestamp !== null && 
         Date.now() - cacheTimestamp < CACHE_DURATION;
}

// =============================================================================
// SERVICE OPERATIONS (using backend API)
// =============================================================================

export const getAllServices = async (): Promise<Service[]> => {
  if (isCacheValid() && servicesCache) {
    return servicesCache;
  }
  
  const services = await fetchActiveServices();
  servicesCache = services;
  cacheTimestamp = Date.now();
  return services;
};

export const getServiceById = async (id: string): Promise<Service | undefined> => {
  const service = await fetchServiceByIdAPI(id);
  return service || undefined;
};

export const getAllTags = async (): Promise<string[]> => {
  return await fetchAllTags();
};

// =============================================================================
// REVIEW OPERATIONS
// =============================================================================

export const getServiceRatings = async (
  serviceId: string
): Promise<ServiceRatingsResponse> => {
  try {
    return await fetchServiceRatings(serviceId);
  } catch (error) {
    console.error(`Failed to load ratings for service ${serviceId}:`, error);
    return {
      ratings: [],
      summary: {
        punctuality: 0,
        friendliness: 0,
        communicative: 0,
        preparedness: 0,
        totalReviews: 0,
      },
    };
  }
};

export const getServiceQuestions = async (
  serviceId: string
): Promise<ServiceQuestion[]> => {
  try {
    return await fetchServiceQuestions(serviceId);
  } catch (error) {
    console.error(`Failed to load questions for service ${serviceId}:`, error);
    return [];
  }
};

export const askServiceQuestion = async (
  serviceId: string,
  content: string
): Promise<ServiceQuestion> => {
  try {
    return await createServiceQuestion(serviceId, content);
  } catch (error) {
    console.error(`Failed to ask question for service ${serviceId}:`, error);
    throw error;
  }
};

export const answerQuestion = async (
  questionId: string,
  content: string
): Promise<ServiceQuestion['answer']> => {
  try {
    return await createQuestionAnswer(questionId, content);
  } catch (error) {
    console.error(`Failed to answer question ${questionId}:`, error);
    throw error;
  }
};