package com.newlecture.resuit.controller;

import java.util.List;
import java.util.UUID;
import java.security.Principal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.newlecture.resuit.dto.CreatePostRequest;
import com.newlecture.resuit.dto.CreateCommentRequest;
import com.newlecture.resuit.dto.CommentResponse;
import com.newlecture.resuit.dto.PostDetailResponse;
import com.newlecture.resuit.dto.PostTimelineResponse;
import com.newlecture.resuit.dto.UpdatePostRequest;
import com.newlecture.resuit.dto.UpdateCommentRequest;
import com.newlecture.resuit.dto.MediaUploadResponse;
import com.newlecture.resuit.service.CommentService;
import com.newlecture.resuit.service.MediaService;
import com.newlecture.resuit.service.PostService;

@RestController
@RequestMapping ("/api/v1/posts")
public class PostsController {

	private final PostService postService;
	private final CommentService commentService;
	private final MediaService mediaService;

	public PostsController(PostService postService, CommentService commentService, MediaService mediaService) {
		this.postService = postService;
		this.commentService = commentService;
		this.mediaService = mediaService;
	}

	@GetMapping
	public Page<PostTimelineResponse> getPost(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return postService.getTimeline(PageRequest.of(page, size));
	}

	@PostMapping("")
	public PostDetailResponse createPost(@jakarta.validation.Valid @RequestBody CreatePostRequest request, Principal principal) {
		return postService.createPost(request, principal.getName());
	}

	@PatchMapping("/{id}")
	public PostDetailResponse updatePost(
			@PathVariable Long id,
			@jakarta.validation.Valid @RequestBody UpdatePostRequest request,
			Principal principal) {
		return postService.updatePost(id, request, principal.getName());
	}

	@DeleteMapping("/{id}")
	public void deletePost(
			@PathVariable Long id,
			Principal principal) {
		postService.deletePost(id, principal.getName());
	}

	@GetMapping("/{id}")
	public PostDetailResponse getPostDetail(@PathVariable Long id) {
		return postService.getPostDetail(id);
	}

	@PostMapping("/{id}/comments")
	public CommentResponse createComment(
			@PathVariable Long id,
			@RequestBody CreateCommentRequest request,
			Principal principal) {
		return commentService.createComment(id, request, principal.getName());
	}

	@PatchMapping("/{id}/comments")
	public CommentResponse updateComment(
			@PathVariable Long id,
			@RequestBody UpdateCommentRequest request,
			Principal principal) {
		return commentService.updateComment(id, request, principal.getName());
	}

	@DeleteMapping("/{id}/comments")
	public void deleteComment(
			@PathVariable Long id,
			Principal principal) {
		commentService.deleteComment(id, principal.getName());
	}

	@GetMapping("/{id}/comments")
	public List<CommentResponse> getComments(@PathVariable Long id) {
		return commentService.getComments(id);
	}

	@PostMapping("/{id}/likes")
	public void likePost(
			@PathVariable Long id,
			Principal principal) {
		postService.likePost(id, principal.getName());
	}

	@DeleteMapping("/{id}/likes")
	public void unlikePost(
			@PathVariable Long id,
			Principal principal) {
		postService.unlikePost(id, principal.getName());
	}

	/*
	* 피드 및 이미지, 영상 업로드쪽은 아직 어떻게 할지 정해지지않아. posts와 일단 같이 관리
	*/
	@GetMapping(value = "/feeds", params = "tab=recommend")
	public Page<PostTimelineResponse> getRecommendedFeed(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return postService.getRecommendedFeed(PageRequest.of(page, size));
	}

	@GetMapping(value = "/feeds", params = "tab=following")
	public Page<PostTimelineResponse> getFollowingFeed(
			@RequestParam UUID userId,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return postService.getFollowingFeed(userId, PageRequest.of(page, size));
	}

	@GetMapping(value = "/feeds", params = "tab=tech")
	public Page<PostTimelineResponse> getTechFeed(
            @RequestParam(required = false) String tag,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return postService.getTechFeed(tag, PageRequest.of(page, size));
	}

	@GetMapping(value = "/feeds", params = "tab=question")
	public Page<PostTimelineResponse> getQuestionFeed(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return postService.getQuestionFeed(PageRequest.of(page, size));
	}

	@GetMapping(value = "/feeds", params = "tab=hashtag")
	public Page<PostTimelineResponse> getHashtagFeed(
			@RequestParam String tag,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return postService.getHashtagFeed(tag, PageRequest.of(page, size));
	}

	/*
	이미지, 영상관련
	이 주석이 남아있는 현재로썬 게시글 당, 영상,이미지 각 1개씩만 업로드 가능
	이것을 개선하려면, 게시글과 미디어를 1:N 관계로 바꾸고, 미디어 업로드시 게시글 id를 받아서 저장하도록 해야함
	엔티티 구조를 바꾸는 작업이 필요, ERD에는 해당 구조를 반영한 테이블은 제작 해놓음

	백엔드 처리 흐름
- 임시 업로드·취소: 기존 API 유지. 임시 파일은 아직 PostMedia에 저장하지 않는다.
- 게시글 작성: 첨부 목록을 받아 최종 경로로 이동하고, 파일마다 PostMedia를 생성
- 게시글 수정: 유지할 첨부, 제거할 첨부, 새 첨부를 구분해 목록과 순서를 갱신
- 게시글 조회: 첨부 목록을 순서대로 DTO에 담아 반환
- 게시글 삭제: 현재의 소프트 삭제 방식을 유지하며, 첨부파일의 실제 정리 시점은 별도로 정함.
파일 이동은 DB 트랜잭션으로 되돌릴 수 없으므로, 저장 도중 실패했을 때 파일을 정리하는 처리도 필요
이건 나중에 구현할래... 토큰없어
	*/
	@PostMapping(value = "/uploads", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public MediaUploadResponse uploadMedia(
			@RequestParam("file") MultipartFile file,
			Principal principal) {
		return mediaService.upload(file, principal.getName());
	}

	@DeleteMapping("/uploads")
	public void discardMedia(
			@RequestParam("path") String path,
			Principal principal) {
		mediaService.discard(path, principal.getName());
	}
}
