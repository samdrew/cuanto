package cuanto

import cuanto.test.TestObjects
import cuanto.test.WordGenerator

class TestRunServiceTests extends GroovyTestCase {

	DataService dataService
	InitializationService initializationService
	TestRunService testRunService
	StatisticService statisticService
	TestObjects to
	WordGenerator wordGen = new WordGenerator()

	@Override
	void setUp() {
		initializationService.initializeAll()
		to = new TestObjects()
		to.dataService = dataService
	}


	void testCalculateTestRunTotals() {
		Project proj = to.getProject()
		proj.testType = TestType.findByName("JUnit")
		dataService.saveDomainObject proj

		def numCases = 11

		TestRun testRun = to.getTestRun(proj)

		if (!testRun.save()) {
			dataService.reportSaveError testRun
		}

		for (x in 1..numCases) {
			TestCase tc = to.getTestCase(proj)
			tc.packageName = "a.b.c"
			dataService.saveDomainObject tc

			TestOutcome outcome = to.getTestOutcome(tc, testRun)
			outcome.duration = 1
			if (x == 2) {
				outcome.testResult = dataService.result("fail")
			} else if (x == 3) {
				outcome.testResult = dataService.result("error")
			} else if (x == 4) {
				outcome.testResult = dataService.result("ignore")
			}

			dataService.saveDomainObject outcome
		}

		statisticService.calculateTestRunStats(testRun.id)

		assertNotNull "results not found", testRun.testRunStatistics
		TestRunStats result = testRun.testRunStatistics
		assertEquals "wrong total tests", 10, result.tests
		assertEquals "wrong failures", 2, result.failed
		assertEquals "wrong passed", 8, result.passed
		assertEquals "wrong avg duration", 1, result.averageDuration
		assertEquals "wrong total duration", 10, result.totalDuration
	}

	void testSearchByTestNote() {
		Project proj = to.project
		proj.testType = TestType.findByName("JUnit")
		if (!proj.save()) {
			dataService.reportSaveError proj
		}

		final int numTests = 10

		def testCases = []
		for (x in 1..numTests) {
			TestCase testCase = to.getTestCase(proj)
			dataService.saveDomainObject testCase
			testCases.add(testCase)
		}

		def testRunOne = to.getTestRun(proj)
		testRunOne.save()

		for (testCase in testCases) {
			TestOutcome out = to.getTestOutcome(testCase, testRunOne)
			dataService.saveDomainObject out
		}

		def testRunTwo = to.getTestRun(proj)
		testRunTwo.save()

		for (testCase in testCases) {
			TestOutcome out = to.getTestOutcome(testCase, testRunTwo)
			dataService.saveDomainObject out
		}

		def runOneOutcomes = testRunService.getOutcomesForTestRun(testRunOne, [includeIgnored: false])
		runOneOutcomes[0].note = "Pacific Ocean Blue"
		runOneOutcomes[0].save()
		runOneOutcomes[1].note = "Lost in the Pacific"
		runOneOutcomes[1].save()

		def runTwoOutcomes = testRunService.getOutcomesForTestRun(testRunTwo, [includeIgnored: false])
		runTwoOutcomes[0].note = "Pacific Lake Blue"
		runTwoOutcomes[0].save()
		runTwoOutcomes[1].note = "Found in the Pacific"
		runTwoOutcomes[1].save()

		def sort = "note"
		def order = "asc"
		def max = 10
		def offset = 0

		def runOneParams = ['sort': sort, 'order': order, 'max': max, 'offset': offset, 'id': testRunOne.id,
			'qry': 'note|Pacific']
		def runTwoParams = ['sort': sort, 'order': order, 'max': max, 'offset': offset, 'id': testRunTwo.id,
			'qry': 'note|Pacific']

		def count = testRunService.countTestOutcomesBySearch(runOneParams)
		assertEquals "Wrong count", 2, count
		count = testRunService.countTestOutcomesBySearch(runTwoParams)
		assertEquals "Wrong count", 2, count

		def searchResults = testRunService.searchTestOutcomes(runOneParams)
		assertEquals "Wrong number of outcomes returned", 2, searchResults.size()
		assertEquals "Wrong outcome", runOneOutcomes[1], searchResults[0]
		assertEquals "Wrong outcome", runOneOutcomes[0], searchResults[1]

		runOneParams['order'] = "desc"
		searchResults = testRunService.searchTestOutcomes(runOneParams)
		assertEquals "Wrong number of outcomes returned", 2, searchResults.size()
		assertEquals "Wrong outcome", runOneOutcomes[0], searchResults[0]
		assertEquals "Wrong outcome", runOneOutcomes[1], searchResults[1]
	}


	void testSearchByTestName() {
		Project proj = to.project
		proj.testType = TestType.findByName("JUnit")
		if (!proj.save()) {
			dataService.reportSaveError proj
		}

		final int numTests = 10

		def testCases = []
		for (x in 1..numTests) {
			TestCase testCase = to.getTestCase(proj)
			dataService.saveDomainObject testCase
			testCases.add(testCase)
		}

		def testRunOne = to.getTestRun(proj)
		testRunOne.save()

		for (testCase in testCases) {
			TestOutcome out = to.getTestOutcome(testCase, testRunOne)
			dataService.saveDomainObject out
		}

		def testRunTwo = to.getTestRun(proj)
		testRunTwo.save()

		for (testCase in testCases) {
			TestOutcome out = to.getTestOutcome(testCase, testRunTwo)
			dataService.saveDomainObject out
		}

		def runOneOutcomes = testRunService.getOutcomesForTestRun(testRunOne, [includeIgnored: false])
		runOneOutcomes[0].testCase.fullName = "a Pacific Ocean Blue"
		runOneOutcomes[0].testCase.save()
		runOneOutcomes[1].testCase.fullName = "b Lost in the Pacific"
		runOneOutcomes[1].testCase.save()

		def runTwoOutcomes = testRunService.getOutcomesForTestRun(testRunTwo, [includeIgnored: false])
		runTwoOutcomes[0].testCase.fullName = "a Pacific Lake Blue"
		runTwoOutcomes[0].testCase.save()
		runTwoOutcomes[1].testCase.fullName = "b Found in the Pacific"
		runTwoOutcomes[1].testCase.save()

		def runOneParams = ['sort': 'name', 'order': 'asc', 'max': 10, 'offset': 0, 'id': testRunOne.id,
			'qry': 'name|Pacific']
		def searchResults = testRunService.searchTestOutcomes(runOneParams)
		assertEquals "Wrong number of outcomes returned", 2, searchResults.size()
		assertEquals "Wrong outcome", runOneOutcomes[0], searchResults[0]
		assertEquals "Wrong outcome", runOneOutcomes[1], searchResults[1]

		runOneParams['order'] = "desc"
		searchResults = testRunService.searchTestOutcomes(runOneParams)
		assertEquals "Wrong number of outcomes returned", 2, searchResults.size()
		assertEquals "Wrong outcome", runOneOutcomes[1], searchResults[0]
		assertEquals "Wrong outcome", runOneOutcomes[0], searchResults[1]
	}


	void testSearchByTestOwner() {
		Project proj = to.project
		proj.testType = TestType.findByName("JUnit")
		if (!proj.save()) {
			dataService.reportSaveError proj
		}

		final int numTests = 10

		def testCases = []
		for (x in 1..numTests) {
			TestCase testCase = to.getTestCase(proj)
			dataService.saveDomainObject testCase
			testCases.add(testCase)
		}

		def testRunOne = to.getTestRun(proj)
		testRunOne.save()

		for (testCase in testCases) {
			TestOutcome out = to.getTestOutcome(testCase, testRunOne)
			dataService.saveDomainObject out
		}

		def testRunTwo = to.getTestRun(proj)
		testRunTwo.save()

		for (testCase in testCases) {
			TestOutcome out = to.getTestOutcome(testCase, testRunTwo)
			dataService.saveDomainObject out
		}

		def runOneOutcomes = testRunService.getOutcomesForTestRun(testRunOne, [includeIgnored: false])
		runOneOutcomes[0].owner = "Pacific Ocean Blue"
		runOneOutcomes[0].save()
		runOneOutcomes[1].owner = "Lost in the Pacific"
		runOneOutcomes[1].save()

		def runTwoOutcomes = testRunService.getOutcomesForTestRun(testRunTwo, [includeIgnored: false])
		runTwoOutcomes[0].owner = "Pacific Lake Blue"
		runTwoOutcomes[0].save()
		runTwoOutcomes[1].owner = "Found in the Pacific"
		runTwoOutcomes[1].save()

		def runOneParams = ['sort': 'owner', 'order': 'asc', 'max': 10, 'offset': 0, 'id': testRunOne.id,
			'qry': "owner|Pacific"]

		def searchResults = testRunService.searchTestOutcomes(runOneParams)
		assertEquals "Wrong number of outcomes returned", 2, searchResults.size()
		assertEquals "Wrong outcome", runOneOutcomes[1], searchResults[0]
		assertEquals "Wrong outcome", runOneOutcomes[0], searchResults[1]

		runOneParams['order'] = 'desc'
		searchResults = testRunService.searchTestOutcomes(runOneParams)
		assertEquals "Wrong number of outcomes returned", 2, searchResults.size()
		assertEquals "Wrong outcome", runOneOutcomes[0], searchResults[0]
		assertEquals "Wrong outcome", runOneOutcomes[1], searchResults[1]
	}

	void testGetProject() {
		def groupNames = ["aa", "bb", "cc"]
		def projectsPerGroup = 3
		def projects = []

		groupNames.each { groupName ->
			def group = to.getProjectGroup(groupName)
			dataService.saveDomainObject(group)
			1.upto(projectsPerGroup) {
				def proj = new Project(name: to.wordGen.getSentence(3), projectKey: to.getProjectKey(),
					projectGroup: group, 'testType': TestType.findByName("JUnit"))
				dataService.saveDomainObject(proj)
				projects << proj
			}
		}

		projects.each { proj ->
			def foundProj = testRunService.getProject(proj.projectKey)
			assertNotNull foundProj
			assertEquals proj, foundProj
		}
	}


	void testCreateAndDeleteTestRun() {

		//todo add test properties
		Project proj = to.project
		dataService.saveDomainObject proj, true 

		def params = [:]
		params.project = proj.projectKey
		params.note = to.wordGen.getSentence(5)

		def links = ["http://gurdy|hurdy", "http://easy|squeezy", "malformed"]

		assertEquals 0, Link.list().size()

		params.link = links
		TestRun createdTr = testRunService.createTestRun(params)

		assertEquals 2, Link.list().size()

		TestRun fetchedTr = TestRun.get(createdTr.id)
		assertEquals "Wrong note", params.note, fetchedTr.note

		assertEquals "Wrong number of links", 2, fetchedTr.links.size()
		assertEquals "http://gurdy", fetchedTr.links[0].url
		assertEquals "hurdy", fetchedTr.links[0].description
		assertEquals "http://easy", fetchedTr.links[1].url
		assertEquals "squeezy", fetchedTr.links[1].description

		dataService.deleteTestRun(fetchedTr)
		assertNull TestRun.get(fetchedTr.id)
		assertEquals 0, Link.list().size()
	}
}

