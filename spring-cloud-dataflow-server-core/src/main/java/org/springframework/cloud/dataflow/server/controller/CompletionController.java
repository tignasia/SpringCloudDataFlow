/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.dataflow.server.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.dataflow.completion.CompletionProposal;
import org.springframework.cloud.dataflow.completion.StreamCompletionProvider;
import org.springframework.cloud.dataflow.core.StreamDefinition;
import org.springframework.cloud.dataflow.rest.resource.CompletionProposalsResource;
import org.springframework.cloud.dataflow.rest.resource.StreamDefinitionResource;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes the DSL completion features of CompletionProvider as a REST API.
 *
 * @author Eric Bottard
 */
@RestController
@RequestMapping("/completions")
@ExposesResourceFor(CompletionProposalsResource.class)
public class CompletionController {

	private final StreamCompletionProvider completionProvider;

	private Assembler assembler = new Assembler();

	@Autowired
	public CompletionController(StreamCompletionProvider completionProvider) {
		this.completionProvider = completionProvider;
	}

	/**
	 * Return a list of possible completions given a prefix string that the user has started typing.
	 *  @param start the amount of text written so far
	 * @param detailLevel the level of detail the user wants in completions, starting at 1.
	 * Higher values request more detail, with values typically in the range [1..5]
	 */
	@RequestMapping(value = "/stream")
	public CompletionProposalsResource completions(
			@RequestParam("start") String start,
			@RequestParam(value = "detailLevel", defaultValue = "1") int detailLevel) {
		return assembler.toResource(completionProvider.complete(start, detailLevel));
	}

	/**
	 * Adapt from the internal CompletionProposal to the client-visible CompletionProposalResource.
	 */
	private List<CompletionProposalsResource.Proposal> toResources(List<CompletionProposal> proposals) {
		List<CompletionProposalsResource.Proposal> result = new ArrayList<>(proposals.size());
		for (CompletionProposal proposal : proposals) {
			result.add(new CompletionProposalsResource.Proposal(proposal.getText(), proposal.getExplanation()));
		}
		return result;
	}

	/**
	 * {@link org.springframework.hateoas.ResourceAssembler} implementation
	 * that converts {@link StreamDefinition}s to {@link StreamDefinitionResource}s.
	 */
	static class Assembler extends ResourceAssemblerSupport<List<CompletionProposal>, CompletionProposalsResource> {

		public Assembler() {
			super(CompletionController.class, CompletionProposalsResource.class);
		}

		@Override
		public CompletionProposalsResource toResource(List<CompletionProposal> proposals) {
			CompletionProposalsResource result = new CompletionProposalsResource();
			for (CompletionProposal proposal : proposals) {
				result.addProposal(proposal.getText(), proposal.getExplanation());
			}
			return result;
		}
	}
}
