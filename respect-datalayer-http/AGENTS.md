# RESPECT respect-datalayer-http guide

This file provides guidance for AI agents working with code in this
module. Always follow the repository guidelines in [../AGENTS.md](../AGENTS.md).

## Module overview
This is an implementation of the [respect-datalayer module](../respect-datalayer)
as an HTTP Client.

You should read [respect-datalayer AGENTS.md](../respect-datalayer/AGENTS.md) before working with
code in this module.

## Datasource Guidance

Example DataSource:

```
class FooDataSourceHttpClient(
    override val schoolUrl: Url,
    override val schoolDirectoryEntryDataSource: SchoolDirectoryEntryDataSource,
    private val httpClient: HttpClient,
    private val tokenProvider: AuthTokenProvider,
    private val json: Json,
): FooDataSource, SchoolUrlBasedDataSource {
    
    private suspend fun GetListParams.urlWithParams(): Url {
        return URLBuilder(xapiEndpointUrl(FooDataSource.ENDPOINT_NAME)).also {
            it.parameters.appendAll(this.toParameters())
        }.build()
    }
    
    override suspend fun get(
        listParams: GetListParams,
        dataLoadParams: DataLoadParams = DataLoadParams(),
    ): DataLoadState<List<Foo>> {
        return httpClient.getAsDataLoadState<List<Foo>>(
            url = listParams.urlWithParams(),
        ) {
            useTokenProvider(tokenProvider)
        }
    }
  
    suspend fun getAsFlow(
        listParams: GetListParams,
        dataLoadParams: DataLoadParams = DataLoadParams(),
    ): Flow<DataLoadState<List<Foo>>> {
         return httpClient.getDataLoadResultAsFlow<List<Foo>>(
            urlFn = {
                listParams.urlWithParams()
            },
            dataLoadParams = dataLoadParams,
         ) {
            useTokenProvider(tokenProvider)
         }
    }

    /** 
     * Write a list of items 
     */
    suspend fun post(
        list: List<Foo>,
    ): DataLoadState<List<String>> {
        return httpClient.post(
            url = xapiEndpointUrl(FooDataSource.ENDPOINT_NAME)
        ) {
            useTokenProvider(tokenProvider)

            contentType(ContentType.Application.Json)
            setBody(list)
        }.toDataLoadState(typeInfo<List<String>>)
    }
}
```

