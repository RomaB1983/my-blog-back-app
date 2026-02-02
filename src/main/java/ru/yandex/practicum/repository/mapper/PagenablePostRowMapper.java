package ru.yandex.practicum.repository.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.NonNull;
import ru.yandex.practicum.model.PagenablePost;
import ru.yandex.practicum.model.Post;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PagenablePostRowMapper implements RowMapper<PagenablePost> {
    @Override
    public PagenablePost mapRow(ResultSet rs, int rowNum) throws SQLException {
        PagenablePost post = (PagenablePost) PostRowMapper.fillPost(rs, new PagenablePost());

        post.setTotalCount(rs.getInt("total_count"));
        post.setCommentsCount(rs.getInt("comments_count"));
        return post;
    }
}
