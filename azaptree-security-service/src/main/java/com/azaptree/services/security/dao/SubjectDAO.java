package com.azaptree.services.security.dao;

import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;

import com.azaptree.services.domain.entity.dao.JDBCEntityDAOSupport;
import com.azaptree.services.domain.entity.dao.Page;
import com.azaptree.services.domain.entity.dao.SearchResults;
import com.azaptree.services.domain.entity.dao.SortField;
import com.azaptree.services.security.domain.Subject;

public class SubjectDAO extends JDBCEntityDAOSupport<Subject> {

	public SubjectDAO(final JdbcTemplate jdbc) {
		super(jdbc);
	}

	@Override
	public UUID create(final Subject entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean delete(final UUID id) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public SearchResults<Subject> findAll(final Page page, final SortField... sort) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SearchResults<Subject> findByExample(final Subject example, final Page page, final SortField... sort) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Subject findById(final UUID id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void update(final Subject entity) {
		// TODO Auto-generated method stub

	}

}
