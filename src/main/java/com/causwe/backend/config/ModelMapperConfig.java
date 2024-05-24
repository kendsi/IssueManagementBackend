package com.causwe.backend.config;

import com.causwe.backend.dto.CommentDTO;
import com.causwe.backend.dto.IssueDTO;
import com.causwe.backend.model.Comment;
import com.causwe.backend.model.Issue;
import com.causwe.backend.model.User;
//import com.causwe.backend.service.UserService;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

    //@Autowired
    //private UserService userService;

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        /*
        Converter<String, User> usernameToUserConverter = new Converter<String, User>() {
            @Override
            public User convert(MappingContext<String, User> context) {
                String username = context.getSource();
                return userService.getUserByUsername(username);
            }
        };
        modelMapper.createTypeMap(IssueDTO.class, Issue.class)
                .addMappings(mapper -> mapper.using(usernameToUserConverter).map(IssueDTO::getReporterUsername, Issue::setReporter))
                .addMappings(mapper -> mapper.using(usernameToUserConverter).map(IssueDTO::getFixerUsername, Issue::setFixer))
                .addMappings(mapper -> mapper.using(usernameToUserConverter).map(IssueDTO::getAssigneeUsername, Issue::setAssignee));
        */

        Converter<User, String> userToUsernameConverter = new Converter<User, String>() {
            @Override
            public String convert(MappingContext<User, String> context) {
                User user = context.getSource();
                return (user != null) ? user.getUsername() : null;
            }
        };

        modelMapper.createTypeMap(Issue.class, IssueDTO.class)
                .addMappings(mapper -> mapper.using(userToUsernameConverter).map(Issue::getReporter, IssueDTO::setReporterUsername))
                .addMappings(mapper -> mapper.using(userToUsernameConverter).map(Issue::getFixer, IssueDTO::setFixerUsername))
                .addMappings(mapper -> mapper.using(userToUsernameConverter).map(Issue::getAssignee, IssueDTO::setAssigneeUsername));

        modelMapper.createTypeMap(Comment.class, CommentDTO.class)
                .addMappings(mapper -> mapper.using(userToUsernameConverter).map(Comment::getUser, CommentDTO::setUsername));

        return modelMapper;
    }
}