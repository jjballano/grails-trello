package com.zulu.trello

import com.foozulu.domain.Action
import com.foozulu.domain.ActionType

import grails.testing.services.ServiceUnitTest
import spock.lang.Specification
import com.stehno.ersatz.ErsatzServer
import com.stehno.ersatz.ContentType
import com.stehno.ersatz.Encoders

class TrelloApiServiceSpec extends Specification implements ServiceUnitTest<TrelloApiService> {

  def setup(){
    config.trello.boards = ['aBoardName']
  }

  def "Retrieve last action from a board"(){
    given:
		  Integer numberOfActions = 1
			List arrayOfActions = [ [
				data: [
					list: [name:'First list'],
					card: [name:'With due date']
					],
				date: '2017-12-21T10:14:27.060Z',
				type: 'updateCard',
				memberCreator: [username:'jjballano']
			] ]
			String uri = "/1/boards/${config.trello.boards.first()}/actions"

			service.BASE_URL = mockResponse(uri, arrayOfActions, ['limit', numberOfActions.toString()])
    when:
			Action action = service.retrieveActions(numberOfActions).first()

    then:
      action.cardName == 'With due date'
      action.user == 'jjballano'
      action.project == 'First list'
      action.date.toString() == 'Thu Dec 21 00:00:00 UTC 2017'
      action.type == ActionType.UPDATE_CARD
  }


	private String mockResponse(String uri, Object response, List params) {
			ErsatzServer ersatz = new ErsatzServer()
			ersatz.expectations {
					get(uri) {
							query(params)
							called(1)
							responder {
									encoder(ContentType.APPLICATION_JSON, response.getClass(), Encoders.json)
									code(200)
									content( response, ContentType.APPLICATION_JSON)
							}
					}
			}
			ersatz.httpUrl
	}
}