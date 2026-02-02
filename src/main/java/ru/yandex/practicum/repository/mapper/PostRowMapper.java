package ru.yandex.practicum.repository.mapper;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.model.Post;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class PostRowMapper implements RowMapper<Post> {

    @Override
    public Post mapRow(ResultSet rs, int rowNum) throws SQLException {
        return fillPost(rs, new Post());
    }

    public static Post fillPost(ResultSet rs, Post post) throws SQLException{
        post.setId(rs.getLong("id"));
        post.setTitle(rs.getString("title"));
        post.setText(rs.getString("text"));

        // Обработка массива тегов (PostgreSQL: тип TEXT[])
        Array tagsArray = rs.getArray("tags");
        String[] stringArray = (String[]) tagsArray.getArray();
        List<String> tagsList = Arrays.asList(stringArray);
        post.setTags(tagsList);
        post.setLikesCount(rs.getInt("likes_count"));

        return post;
    }
}
