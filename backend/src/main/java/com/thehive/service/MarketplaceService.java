package com.thehive.service;

import com.thehive.exception.ResourceNotFoundException;
import com.thehive.model.dto.AuthorDTO;
import com.thehive.model.dto.CreateOfferRequest;
import com.thehive.model.dto.CreateRequestRequest;
import com.thehive.model.dto.CreateQuestionRequest;
import com.thehive.model.dto.CreateAnswerRequest;
import com.thehive.model.dto.OfferDTO;
import com.thehive.model.dto.RequestDTO;
import com.thehive.model.dto.ServiceAnswerDTO;
import com.thehive.model.dto.ServiceDTO;
import com.thehive.model.dto.ServiceQuestionDTO;
import com.thehive.model.dto.ServiceRatingDTO;
import com.thehive.model.dto.ServiceRatingSummaryDTO;
import com.thehive.model.dto.ServiceRatingsResponseDTO;
import com.thehive.model.entity.Offer;
import com.thehive.model.entity.Request;
import com.thehive.model.entity.SemanticTag;
import com.thehive.model.entity.Answer;
import com.thehive.model.entity.Question;
import com.thehive.model.entity.Rating;
import com.thehive.model.entity.User;
import com.thehive.model.enums.ItemStatus;
import com.thehive.repository.OfferRepository;
import com.thehive.repository.RequestRepository;
import com.thehive.repository.QuestionRepository;
import com.thehive.repository.AnswerRepository;
import com.thehive.repository.RatingRepository;
import com.thehive.repository.UserRepository;
import com.thehive.repository.SemanticTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MarketplaceService {

    private final OfferRepository offerRepository;
    private final RequestRepository requestRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;
    private final SemanticTagRepository semanticTagRepository;

    @Transactional(readOnly = true)
    public List<OfferDTO> getAllOffers() {
        List<Offer> offers = offerRepository.findAll();
        return offers.stream()
                .map(this::convertToOfferDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OfferDTO> getActiveOffers() {
        List<Offer> offers = offerRepository.findByStatus(ItemStatus.ACTIVE);
        return offers.stream()
                .map(this::convertToOfferDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OfferDTO getOfferById(Integer id) {
        Offer offer = offerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found with id: " + id));
        return convertToOfferDTO(offer);
    }

    @Transactional
    public OfferDTO createOffer(CreateOfferRequest request, Integer providerId) {
        // Get the provider user
        User provider = userRepository.findById(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + providerId));

        // Create new offer
        Offer offer = new Offer();
        offer.setProvider(provider);
        offer.setTitle(request.getTitle());
        offer.setDescription(request.getDescription());
        offer.setDurationHours(request.getDurationHours());
        offer.setStartDate(request.getStartDate());
        offer.setEndDate(request.getEndDate());
        offer.setProvince(request.getProvince());
        offer.setDistrict(request.getDistrict());
        offer.setGeohash(request.getGeohash());
        offer.setStatus(ItemStatus.ACTIVE);

        // Handle image URLs
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            offer.setImageUrls(String.join(",", request.getImageUrls()));
        }

        // Handle tags
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            for (String tagName : request.getTags()) {
                SemanticTag tag = semanticTagRepository.findByName(tagName)
                        .orElseGet(() -> {
                            SemanticTag newTag = new SemanticTag();
                            newTag.setName(tagName);
                            return semanticTagRepository.save(newTag);
                        });
                offer.getTags().add(tag);
            }
        }

        // Save the offer
        Offer savedOffer = offerRepository.save(offer);

        // Convert to DTO and return
        return convertToOfferDTO(savedOffer);
    }

    @Transactional
    public RequestDTO createRequest(CreateRequestRequest request, Integer seekerId) {
        // Get the seeker user
        User seeker = userRepository.findById(seekerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + seekerId));

        // Create new request
        Request newRequest = new Request();
        newRequest.setSeeker(seeker);
        newRequest.setTitle(request.getTitle());
        newRequest.setDescription(request.getDescription());
        newRequest.setDurationHours(request.getDurationHours());
        newRequest.setStartDate(request.getStartDate());
        newRequest.setEndDate(request.getEndDate());
        newRequest.setProvince(request.getProvince());
        newRequest.setDistrict(request.getDistrict());
        newRequest.setGeohash(request.getGeohash());
        newRequest.setStatus(ItemStatus.ACTIVE);

        // Handle image URLs
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            newRequest.setImageUrls(String.join(",", request.getImageUrls()));
        }

        // Handle tags
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            for (String tagName : request.getTags()) {
                SemanticTag tag = semanticTagRepository.findByName(tagName)
                        .orElseGet(() -> {
                            SemanticTag newTag = new SemanticTag();
                            newTag.setName(tagName);
                            return semanticTagRepository.save(newTag);
                        });
                newRequest.getTags().add(tag);
            }
        }

        // Save the request
        Request savedRequest = requestRepository.save(newRequest);

        // Convert to DTO and return
        return convertToRequestDTO(savedRequest);
    }

    @Transactional(readOnly = true)
    public List<RequestDTO> getAllRequests() {
        List<Request> requests = requestRepository.findAll();
        return requests.stream()
                .map(this::convertToRequestDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RequestDTO> getActiveRequests() {
        List<Request> requests = requestRepository.findByStatus(ItemStatus.ACTIVE);
        return requests.stream()
                .map(this::convertToRequestDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RequestDTO getRequestById(Integer id) {
        Request request = requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with id: " + id));
        return convertToRequestDTO(request);
    }

    @Transactional(readOnly = true)
    public List<ServiceDTO> getAllServices() {
        List<ServiceDTO> services = new ArrayList<>();
        
        // Get all offers and convert to ServiceDTO
        List<Offer> offers = offerRepository.findAll();
        services.addAll(offers.stream()
                .map(this::convertOfferToServiceDTO)
                .collect(Collectors.toList()));
        
        // Get all requests and convert to ServiceDTO
        List<Request> requests = requestRepository.findAll();
        services.addAll(requests.stream()
                .map(this::convertRequestToServiceDTO)
                .collect(Collectors.toList()));
        
        return services;
    }

    @Transactional(readOnly = true)
    public List<ServiceDTO> getActiveServices() {
        List<ServiceDTO> services = new ArrayList<>();
        
        // Get active offers and convert to ServiceDTO
        List<Offer> offers = offerRepository.findByStatus(ItemStatus.ACTIVE);
        services.addAll(offers.stream()
                .map(this::convertOfferToServiceDTO)
                .collect(Collectors.toList()));
        
        // Get active requests and convert to ServiceDTO
        List<Request> requests = requestRepository.findByStatus(ItemStatus.ACTIVE);
        services.addAll(requests.stream()
                .map(this::convertRequestToServiceDTO)
                .collect(Collectors.toList()));
        
        return services;
    }

    @Transactional(readOnly = true)
    public ServiceDTO getServiceById(Integer id) {
        // Try to find as offer first
        var offer = offerRepository.findById(id);
        if (offer.isPresent()) {
            return convertOfferToServiceDTO(offer.get());
        }
        
        // Try to find as request
        var request = requestRepository.findById(id);
        if (request.isPresent()) {
            return convertRequestToServiceDTO(request.get());
        }
        
        // If neither found, throw exception
        throw new ResourceNotFoundException("Service not found with id: " + id);
    }

    @Transactional(readOnly = true)
    public List<ServiceQuestionDTO> getQuestionsForService(Integer serviceId) {
        List<Question> questions;

        if (offerRepository.existsById(serviceId)) {
            questions = questionRepository.findByOfferId(serviceId);
        } else if (requestRepository.existsById(serviceId)) {
            questions = questionRepository.findByRequestId(serviceId);
        } else {
            throw new ResourceNotFoundException("Service not found with id: " + serviceId);
        }

        return questions.stream()
                .map(this::convertToServiceQuestionDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ServiceQuestionDTO createQuestionForService(Integer serviceId, CreateQuestionRequest request, Integer askerId) {
        // Validate request content
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Question content cannot be null or empty");
        }

        // Get the asker user
        User asker = userRepository.findById(askerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + askerId));

        // Create new question
        Question question = new Question();
        question.setAsker(asker);
        question.setContent(request.getContent());

        // Determine if service is an offer or request and set accordingly
        var offer = offerRepository.findById(serviceId);
        if (offer.isPresent()) {
            question.setOffer(offer.get());
        } else {
            var requestEntity = requestRepository.findById(serviceId);
            if (requestEntity.isPresent()) {
                question.setRequest(requestEntity.get());
            } else {
                throw new ResourceNotFoundException("Service not found with id: " + serviceId);
            }
        }

        // Save the question
        Question savedQuestion = questionRepository.save(question);

        // Convert to DTO and return
        return convertToServiceQuestionDTO(savedQuestion);
    }

    @Transactional
    public ServiceAnswerDTO createAnswerForQuestion(Integer questionId, CreateAnswerRequest request, Integer responderId) {
        // Validate request content
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Answer content cannot be null or empty");
        }

        // Get the question
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + questionId));

        // Check if question already has an answer
        if (question.getAnswer() != null) {
            throw new IllegalStateException("Question already has an answer");
        }

        // Get the responder user
        User responder = userRepository.findById(responderId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + responderId));

        // Create new answer
        Answer answer = new Answer();
        answer.setQuestion(question);
        answer.setResponder(responder);
        answer.setContent(request.getContent());

        // Save the answer
        Answer savedAnswer = answerRepository.save(answer);

        // Convert to DTO and return
        return convertToServiceAnswerDTO(savedAnswer);
    }

    @Transactional(readOnly = true)
    public ServiceRatingsResponseDTO getRatingsForService(Integer serviceId) {
        if (offerRepository.existsById(serviceId)) {
            Offer offer = offerRepository.findById(serviceId)
                    .orElseThrow(() -> new ResourceNotFoundException("Offer not found with id: " + serviceId));
            List<Rating> ratings = ratingRepository.findByHandshakeOfferId(serviceId);
            List<ServiceRatingDTO> ratingDTOs = ratings.stream()
                    .map(rating -> convertToServiceRatingDTO(rating, offer.getId(), offer.getTitle()))
                    .collect(Collectors.toList());

            ServiceRatingSummaryDTO summary = calculateRatingSummary(ratings);
            return new ServiceRatingsResponseDTO(ratingDTOs, summary);
        }

        if (requestRepository.existsById(serviceId)) {
            Request request = requestRepository.findById(serviceId)
                    .orElseThrow(() -> new ResourceNotFoundException("Request not found with id: " + serviceId));
            List<Rating> ratings = ratingRepository.findByHandshakeRequestId(serviceId);
            List<ServiceRatingDTO> ratingDTOs = ratings.stream()
                    .map(rating -> convertToServiceRatingDTO(rating, request.getId(), request.getTitle()))
                    .collect(Collectors.toList());

            ServiceRatingSummaryDTO summary = calculateRatingSummary(ratings);
            return new ServiceRatingsResponseDTO(ratingDTOs, summary);
        }

        throw new ResourceNotFoundException("Service not found with id: " + serviceId);
    }

    // Helper methods to convert entities to DTOs
    private OfferDTO convertToOfferDTO(Offer offer) {
        OfferDTO dto = new OfferDTO();
        dto.setId(offer.getId());
        dto.setTitle(offer.getTitle());
        dto.setDescription(offer.getDescription());
        dto.setDurationHours(offer.getDurationHours());
        dto.setStartDate(offer.getStartDate());
        dto.setEndDate(offer.getEndDate());
        dto.setProvince(offer.getProvince());
        dto.setDistrict(offer.getDistrict());
        dto.setGeohash(offer.getGeohash());
        dto.setStatus(offer.getStatus());
        dto.setCreatedAt(offer.getCreatedAt());
        dto.setUpdatedAt(offer.getUpdatedAt());
        dto.setProvider(convertToAuthorDTO(offer.getProvider()));
        dto.setTags(offer.getTags().stream()
                .map(SemanticTag::getName)
                .collect(Collectors.toList()));
        dto.setImageUrls(parseImageUrls(offer.getImageUrls()));
        return dto;
    }

    private RequestDTO convertToRequestDTO(Request request) {
        RequestDTO dto = new RequestDTO();
        dto.setId(request.getId());
        dto.setTitle(request.getTitle());
        dto.setDescription(request.getDescription());
        dto.setDurationHours(request.getDurationHours());
        dto.setStartDate(request.getStartDate());
        dto.setEndDate(request.getEndDate());
        dto.setProvince(request.getProvince());
        dto.setDistrict(request.getDistrict());
        dto.setGeohash(request.getGeohash());
        dto.setStatus(request.getStatus());
        dto.setCreatedAt(request.getCreatedAt());
        dto.setUpdatedAt(request.getUpdatedAt());
        dto.setSeeker(convertToAuthorDTO(request.getSeeker()));
        dto.setTags(request.getTags().stream()
                .map(SemanticTag::getName)
                .collect(Collectors.toList()));
        dto.setImageUrls(parseImageUrls(request.getImageUrls()));
        return dto;
    }

    private ServiceQuestionDTO convertToServiceQuestionDTO(Question question) {
        ServiceQuestionDTO dto = new ServiceQuestionDTO();
        dto.setId(question.getId());
        dto.setAuthor(convertToAuthorDTO(question.getAsker()));
        dto.setContent(question.getContent());
        dto.setCreatedAt(question.getCreatedAt());

        Answer answer = question.getAnswer();
        if (answer != null) {
            dto.setAnswer(convertToServiceAnswerDTO(answer));
        }

        return dto;
    }

    private ServiceAnswerDTO convertToServiceAnswerDTO(Answer answer) {
        ServiceAnswerDTO dto = new ServiceAnswerDTO();
        dto.setId(answer.getId());
        dto.setResponder(convertToAuthorDTO(answer.getResponder()));
        dto.setContent(answer.getContent());
        dto.setCreatedAt(answer.getCreatedAt());
        return dto;
    }

    private ServiceRatingDTO convertToServiceRatingDTO(Rating rating) {
        return convertToServiceRatingDTO(rating, null, null);
    }

    private ServiceRatingDTO convertToServiceRatingDTO(Rating rating, Integer serviceId, String serviceTitle) {
        ServiceRatingDTO dto = new ServiceRatingDTO();
        dto.setId(rating.getId());
        dto.setRater(convertToAuthorDTO(rating.getRater()));
        dto.setPunctuality(rating.getPunctuality());
        dto.setFriendliness(rating.getFriendliness());
        dto.setCommunicative(rating.getCommunicative());
        dto.setPreparedness(rating.getPreparedness());
        dto.setComment(rating.getComment());
        dto.setCreatedAt(rating.getCreatedAt());
        dto.setServiceId(serviceId);
        dto.setServiceTitle(serviceTitle);
        return dto;
    }

    private ServiceRatingSummaryDTO calculateRatingSummary(List<Rating> ratings) {
        if (ratings.isEmpty()) {
            return new ServiceRatingSummaryDTO(0, 0, 0, 0, 0);
        }

        int total = ratings.size();
        double punctualityAvg = ratings.stream()
                .mapToInt(r -> safeRatingValue(r.getPunctuality()))
                .average()
                .orElse(0);
        double friendlinessAvg = ratings.stream()
                .mapToInt(r -> safeRatingValue(r.getFriendliness()))
                .average()
                .orElse(0);
        double communicativeAvg = ratings.stream()
                .mapToInt(r -> safeRatingValue(r.getCommunicative()))
                .average()
                .orElse(0);
        double preparednessAvg = ratings.stream()
                .mapToInt(r -> safeRatingValue(r.getPreparedness()))
                .average()
                .orElse(0);

        return new ServiceRatingSummaryDTO(
                roundToSingleDecimal(punctualityAvg),
                roundToSingleDecimal(friendlinessAvg),
                roundToSingleDecimal(communicativeAvg),
                roundToSingleDecimal(preparednessAvg),
                total
        );
    }

    private double roundToSingleDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private int safeRatingValue(Integer value) {
        return value != null ? value : 0;
    }

    private ServiceDTO convertOfferToServiceDTO(Offer offer) {
        ServiceDTO dto = new ServiceDTO();
        dto.setId(offer.getId());
        dto.setType("OFFER");
        dto.setTitle(offer.getTitle());
        dto.setDescription(offer.getDescription());
        dto.setTimebank(offer.getDurationHours());
        dto.setStartDate(offer.getStartDate());
        dto.setEndDate(offer.getEndDate());
        dto.setProvince(offer.getProvince());
        dto.setDistrict(offer.getDistrict());
        dto.setGeohash(offer.getGeohash());
        dto.setLocation(formatLocation(offer.getProvince(), offer.getDistrict()));
        dto.setStatus(offer.getStatus() != null ? offer.getStatus().toString() : "ACTIVE");
        dto.setCreatedAt(offer.getCreatedAt());
        dto.setUpdatedAt(offer.getUpdatedAt());
        dto.setPoster(convertToAuthorDTO(offer.getProvider()));
        dto.setTags(offer.getTags().stream()
                .map(SemanticTag::getName)
                .collect(Collectors.toList()));
        dto.setImageUrls(parseImageUrls(offer.getImageUrls()));
        return dto;
    }

    private ServiceDTO convertRequestToServiceDTO(Request request) {
        ServiceDTO dto = new ServiceDTO();
        dto.setId(request.getId());
        dto.setType("REQUEST");
        dto.setTitle(request.getTitle());
        dto.setDescription(request.getDescription());
        dto.setTimebank(request.getDurationHours());
        dto.setStartDate(request.getStartDate());
        dto.setEndDate(request.getEndDate());
        dto.setProvince(request.getProvince());
        dto.setDistrict(request.getDistrict());
        dto.setGeohash(request.getGeohash());
        dto.setLocation(formatLocation(request.getProvince(), request.getDistrict()));
        dto.setStatus(request.getStatus() != null ? request.getStatus().toString() : "ACTIVE");
        dto.setCreatedAt(request.getCreatedAt());
        dto.setUpdatedAt(request.getUpdatedAt());
        dto.setPoster(convertToAuthorDTO(request.getSeeker()));
        dto.setTags(request.getTags().stream()
                .map(SemanticTag::getName)
                .collect(Collectors.toList()));
        dto.setImageUrls(parseImageUrls(request.getImageUrls()));
        return dto;
    }

    private AuthorDTO convertToAuthorDTO(User user) {
        AuthorDTO dto = new AuthorDTO();
        dto.setId(user.getId());
        dto.setName(user.getName() != null ? user.getName() : user.getEmail());
        dto.setAvatar(user.getAvatarUrl());
        dto.setBio(user.getBio());
        dto.setProvince(user.getProvince());
        dto.setDistrict(user.getDistrict());
        dto.setBalanceHours(user.getBalanceHours());
        
        if (user.getUserBadges() != null && !user.getUserBadges().isEmpty()) {
            // Get the most recent badge based on earned_at timestamp
            var latestBadge = user.getUserBadges().stream()
                .max((ub1, ub2) -> ub1.getEarnedAt().compareTo(ub2.getEarnedAt()))
                .map(userBadge -> userBadge.getBadge().getName())
                .orElse("Newcomer");
            dto.setBadge(latestBadge);
        } else {
            // Default badge for users with no badges
            dto.setBadge("Newcomer");
        }
        
        return dto;
    }

    private String formatLocation(String province, String district) {
        if (province == null && district == null) {
            return "Unknown";
        }
        if (province != null && district != null) {
            return district + ", " + province;
        }
        if (province != null) {
            return province;
        }
        return district;
    }

    private List<String> parseImageUrls(String imageUrls) {
        if (imageUrls == null || imageUrls.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return List.of(imageUrls.split(","));
    }

    @Transactional(readOnly = true)
    public List<OfferDTO> getOffersByProvider(Integer providerId) {
        List<Offer> offers = offerRepository.findByProviderId(providerId);
        return offers.stream()
                .map(this::convertToOfferDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RequestDTO> getRequestsBySeeker(Integer seekerId) {
        List<Request> requests = requestRepository.findBySeekerId(seekerId);
        return requests.stream()
                .map(this::convertToRequestDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ServiceDTO> getUserServices(Integer userId) {
        List<ServiceDTO> services = new ArrayList<>();
        
        // Get user's offers and convert to ServiceDTO
        List<Offer> offers = offerRepository.findByProviderId(userId);
        services.addAll(offers.stream()
                .map(this::convertOfferToServiceDTO)
                .collect(Collectors.toList()));
        
        // Get user's requests and convert to ServiceDTO
        List<Request> requests = requestRepository.findBySeekerId(userId);
        services.addAll(requests.stream()
                .map(this::convertRequestToServiceDTO)
                .collect(Collectors.toList()));
        
        return services;
    }
}

