import org.apache.commons.validator.routines.UrlValidator
import org.jsoup.Jsoup

class ApplicationMain {

    static void main(String[] args) {
        def expectedSite = "http://kpfu.ru/"

        String[] byX = new String[100]
        String[] byY = new String[100]

        byte[][] matrix = new byte[100][100]

        double[][] chances = new double[100][100]

        double[] pageRanks = new double[100]
        double[] newPageRanks = new double[100]

        def toNormalUrlForm = { String uri, String parentUrl ->
            return new UrlValidator().isValid(uri) ? uri : parentUrl.endsWith("/") ?
                    parentUrl + uri.substring(1, uri.length()) : parentUrl + uri
        }

        def isValidUri = { String uri, String parentUrl ->
            return !uri.startsWith("//") && !uri.contains("#") &&
                    (uri.startsWith("/") || uri.startsWith("?") || uri.startsWith(parentUrl))
        }

        def getAllLinks = { String url ->
            try {
                return Jsoup.parse(new URL(url).text)
                        .select("a[href]")
//                    .findAll { it -> isValidUri(it.attributes().get("href"), url) }
                        .collect { it -> toNormalUrlForm(it.attributes().get("href"), url).toString() }
            } catch (Exception e) {
                return new String[0]
            }
        }



        String[] initialColumns = getAllLinks(expectedSite)
        println initialColumns.size()


        (0..99).each { it -> byX[it] = byY[it] = initialColumns[it] }
        100.times { i ->
            100.times { j ->
                matrix[i][j] = 0
                chances[i][j] = 0
            }
            pageRanks[i] = 0.01
            newPageRanks[i] = 0
        }

        Date start = new Date()
        long startMS = start.getTime()


        Date finish = new Date()
        long finishMS = finish.getTime()

        Date startP = new Date()
        long startPMS = startP.getTime()

        Thread[] threads = new Thread[100]
        (0..99).each { i ->
            threads[i] = Thread.start {
                String[] links = getAllLinks(initialColumns[i])
                links.each { link ->
                    (0..99).each { j ->
                        matrix[i][j] = link == initialColumns[j] ? 1 : matrix[i][j]
                    }
                }
            }
            println i
        }
        threads.each {thread ->
            thread.join()
        }

        Date finishP = new Date()
        long finishPMS = finishP.getTime()


        100.times { i ->
            100.times { j -> print matrix[i][j] }
            println ' - ' + initialColumns[i]
        }

        println 'chances:'
        println ' '
        (0..99).each { i ->
            int linksCount = 0
            (0..99).each { j ->
                linksCount += matrix[i][j]
            }
            (0..99).each { j ->
                chances[i][j] = linksCount == 0 ? 0 : matrix[i][j] / linksCount
                print chances[i][j] + ' '
            }
            println ' '
        }


        def findPageRanks = { int stepen ->
            stepen.times { n ->
                (0..99).each { i ->
                    (0..99).each { j ->
                        newPageRanks[i] += chances[j][i] * pageRanks[j]
                    }
                }
                println newPageRanks.toString()

                (0..99).each { i ->
                    pageRanks[i] = newPageRanks[i]
                    newPageRanks[i] = 0
                }

                println pageRanks.toString()
                println newPageRanks.toString()
                println n

            }
        }

        //task2
        findPageRanks(10)

        double maxPageRank = -1
        int maxPRindex = 0
        (0..99).each { i ->
            maxPageRank = pageRanks[i] > maxPageRank ? pageRanks[i] : maxPageRank
            maxPRindex = pageRanks[i] > maxPageRank ? i : maxPRindex
        }
        println initialColumns[maxPRindex] + ' : ' + maxPageRank

        long withoutP = finishMS-startMS
        long withP = finishPMS-startPMS
        println "without paralel: " + withoutP
        println "with paralel: " + withP


        //task3
        byte[] A = new byte[10000]
        int[] NI = new int[10000]
        int[] NJ = new int[10000]

        (0..99).each { i->
            (0..99).each { j->
                if(matrix[i][j] !=0 ){
                    A[i*100+j]=matrix[i][j]
                    NI[i*100+j]=i
                    NJ[i*100+j]=j
                }
            }
        }


        println A.toString()
        println NI.toString()
        println NJ.toString()
    }

}
