package com.newlecture.resuit.controller;

import com.newlecture.resuit.dto.PinnedResumeRequest;
import com.newlecture.resuit.dto.DisplayedBadgeRequest;
import com.newlecture.resuit.dto.FollowStatusResponse;
import com.newlecture.resuit.dto.MyProfileResponse;
import com.newlecture.resuit.dto.PageResponse;
import com.newlecture.resuit.dto.ProfileFeedbackResponse;
import com.newlecture.resuit.dto.ProfilePostResponse;
import com.newlecture.resuit.dto.ProfileResponse;
import com.newlecture.resuit.dto.UpdateProfileRequest;
import com.newlecture.resuit.dto.ResumeVisibilityRequest;
import com.newlecture.resuit.dto.BlogIntegrationRequest;
import com.newlecture.resuit.dto.BlogPostResponse;
import com.newlecture.resuit.dto.GithubContributionsResponse;
import com.newlecture.resuit.dto.IntegrationResponse;
import com.newlecture.resuit.dto.PinnedReposRequest;
import com.newlecture.resuit.dto.RepoResponse;
import com.newlecture.resuit.service.CurrentUserResolver;
import com.newlecture.resuit.service.GithubContributionService;
import com.newlecture.resuit.service.ProfileIntegrationService;
import com.newlecture.resuit.service.ProfileService;
import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping ("/api/v1/users")
public class UserController {
	private final ProfileService profiles;
	private final CurrentUserResolver currentUser;
	private final ProfileIntegrationService integrations;
	private final GithubContributionService githubContributions;

	public UserController(ProfileService profiles, CurrentUserResolver currentUser,
			ProfileIntegrationService integrations, GithubContributionService githubContributions) {
		this.profiles = profiles;
		this.currentUser = currentUser;
		this.integrations = integrations;
		this.githubContributions = githubContributions;
	}

	@GetMapping("/me")
	public MyProfileResponse getMyProfile(@RequestHeader(value = "X-Username", required = false) String username) {
		return profiles.getMyProfile(currentUser.requireUserId(username));
	}

	@PatchMapping("/me")
	public MyProfileResponse updateMyProfile(
			@RequestHeader(value = "X-Username", required = false) String username,
			@RequestBody UpdateProfileRequest request) {
		return profiles.updateMyProfile(currentUser.requireUserId(username), request);
	}

	@GetMapping("/{username}")
	public ProfileResponse getUserProfile(@PathVariable String username,
			@RequestHeader(value = "X-Username", required = false) String viewer) {
		return profiles.getPublicProfile(username, currentUser.optionalUserId(viewer));
	}

	@PostMapping("/{username}/follow")
	public FollowStatusResponse followUser(
			@PathVariable String username,
			@RequestHeader(value = "X-Username", required = false) String viewer) {
		return profiles.follow(currentUser.requireUserId(viewer), username);
	}

	@DeleteMapping("/{username}/follow")
	public FollowStatusResponse unfollowUser(
			@PathVariable String username,
			@RequestHeader(value = "X-Username", required = false) String viewer) {
		return profiles.unfollow(currentUser.requireUserId(viewer), username);
	}

	@GetMapping("/{username}/tweets")
	public PageResponse<ProfilePostResponse> getUserTweets(
			@PathVariable String username,
			@RequestParam(required = false) String type,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return profiles.getPosts(username, type, page, size);
	}

	@GetMapping("/{username}/feedbacks")
	public PageResponse<ProfileFeedbackResponse> getUserFeedbacks(
			@PathVariable String username,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return profiles.getFeedbacks(username, page, size);
	}

	@PutMapping("/me/pinned-resume")
	public ProfileResponse updatePinnedResume(
			@RequestHeader(value = "X-Username", required = false) String username,
			@RequestBody PinnedResumeRequest request) {
		return profiles.pinResume(currentUser.requireUserId(username), request);
	}

	@PutMapping("/me/resumes/{resumeId}/visibility")
	public MyProfileResponse updateResumeVisibility(
			@RequestHeader(value = "X-Username", required = false) String username,
			@PathVariable Long resumeId,
			@RequestBody ResumeVisibilityRequest request) {
		return profiles.setResumeVisibility(currentUser.requireUserId(username), resumeId, request);
	}

	@PutMapping("/me/displayed-badge")
	public ProfileResponse updateDisplayedBadge(
			@RequestHeader(value = "X-Username", required = false) String username,
			@RequestBody DisplayedBadgeRequest request) {
		return profiles.displayBadge(currentUser.requireUserId(username), request);
	}

	@GetMapping("/{username}/github/repos")
	public List<RepoResponse> getGithubRepositories(
			@PathVariable String username,
			@RequestParam(required = false) Boolean pinned) {
		return integrations.getGithubRepos(username, pinned);
	}

	@GetMapping("/{username}/github/contributions")
	public GithubContributionsResponse getGithubContributions(@PathVariable String username) {
		return githubContributions.getContributions(username);
	}

	@PutMapping("/me/github/pinned-repos")
	public List<RepoResponse> updatePinnedRepositories(
			@RequestHeader(value = "X-Username", required = false) String username,
			@RequestBody PinnedReposRequest request) {
		return integrations.setPinnedRepos(currentUser.requireUserId(username), request);
	}

	@GetMapping("/{username}/blog-posts")
	public List<BlogPostResponse> getBlogPosts(
			@PathVariable String username,
			@RequestParam(defaultValue = "5") int size) {
		return integrations.getBlogPosts(username, size);
	}

	@GetMapping("/me/integrations")
	public List<IntegrationResponse> getIntegrations(
			@RequestHeader(value = "X-Username", required = false) String username) {
		return integrations.getIntegrations(currentUser.requireUserId(username));
	}

	@PutMapping("/me/integrations/blog")
	public IntegrationResponse updateBlogIntegration(
			@RequestHeader(value = "X-Username", required = false) String username,
			@RequestBody BlogIntegrationRequest request) {
		return integrations.connectBlog(currentUser.requireUserId(username), request);
	}

	@DeleteMapping("/me/integrations/{provider}")
	public IntegrationResponse disconnectIntegration(
			@RequestHeader(value = "X-Username", required = false) String username,
			@PathVariable String provider) {
		return integrations.disconnect(currentUser.requireUserId(username), provider);
	}
}
