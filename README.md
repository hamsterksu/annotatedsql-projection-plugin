annotatedsql-projection-plugin
==============================

Plugin for annotatedsql [Android-AnnotatedSQL][1]. Generate projections and uri 

Easy way to generate projection and columns indexes for it.
Just add @Projection to your @Table and get class with all necessary data in Projections class

Code in your table declaration 
  
        @Projection
        String PROJECTION_ALL = "All";
  
  
Result: inner class in Projections

	public static class PostTableQuery{

        public final static Uri CONTENT_URI = PostsProvider.contentUri(PostTable.CONTENT_URI);
        public final static Uri CONTENT_URI_NO_NOTIFY = PostsProvider.contentUriNoNotify(PostTable.CONTENT_URI);

        public static class All{

            public static final String[] PROJECTION = new String[]{
                PostTable.ID,
                PostTable.TEXT
            };

            public static final int INDEX_ID = 0;
            public static final int INDEX_TEXT = 1;
        }
    }

*How to add to your project?*
----------------
Very easy way - just use [aptlibs][2] 

	aptlibs {

		annotatedSql {
			version = "${asVersion}"
			logLevel = 'INFO'
			plugins {
				projectionPlugin{
					version = '1.0.+'
					dependencies = ["com.github.hamsterksu:projection-plugin-api:${version}", "com.github.hamsterksu:projection-plugin-processor:${version}"]
					plugin = "com.hamsterksu.asql.projections.ProjectionPlugin"
				}
			}
		}
	}

  [1]: https://github.com/hamsterksu/Android-AnnotatedSQL
  [2]: https://github.com/hamsterksu/android-aptlibs-gradle-plugin
