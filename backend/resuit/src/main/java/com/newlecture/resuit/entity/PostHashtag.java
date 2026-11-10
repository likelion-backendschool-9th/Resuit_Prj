package com.newlecture.resuit.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "post_hashtags")
@IdClass(PostHashtagId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostHashtag {

	@Id
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "post_id", nullable = false)
	private Post post;

	@Id
	@Column(name = "tag", nullable = false, length = 50)
	private String tag;

	public PostHashtag(Post post, String tag) {
		this.post = post;
		this.tag = tag;
	}
}
