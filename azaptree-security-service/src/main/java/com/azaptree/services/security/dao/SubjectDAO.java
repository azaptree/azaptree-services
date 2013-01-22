package com.azaptree.services.security.dao;

import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.azaptree.services.domain.entity.dao.JDBCEntityDAOSupport;
import com.azaptree.services.domain.entity.dao.Page;
import com.azaptree.services.domain.entity.dao.SearchResults;
import com.azaptree.services.domain.entity.dao.SortField;
import com.azaptree.services.security.domain.Subject;

@Repository
public class SubjectDAO extends JDBCEntityDAOSupport<Subject> {

	public SubjectDAO(final JdbcTemplate jdbc) {
		super(jdbc);
	}

	@Override
	@Transactional
	public UUID create(final Subject entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Transactional
	public boolean delete(final UUID id) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	@Transactional
	public SearchResults<Subject> findAll(final Page page, final SortField... sort) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Transactional
	public SearchResults<Subject> findByExample(final Subject example, final Page page, final SortField... sort) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Transactional
	public Subject findById(final UUID id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Transactional
	public void update(final Subject entity) {
		// TODO Auto-generated method stub

	}

}
