package com.github.mideo.cassandra.connector.repository

import com.datastax.driver.core._
import com.datastax.driver.mapping.Mapper
import com.github.mideo.cassandra.connector.{CassandraConnectorTest, TestAddress, TestUser}
import com.google.common.util.concurrent.ListenableFuture
import org.mockito.Matchers.any
import org.mockito.Mockito._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._


class ConnectedKeyspaceTest extends CassandraConnectorTest {
  implicit val  cluster: Cluster = mock[Cluster]
  val listenableFuture: ListenableFuture[Session] = mock[ListenableFuture[Session]]
  val closeFuture: CloseFuture = mock[CloseFuture]

  val session: Session = mock[Session]
  val configuration: Configuration = mock[Configuration]
  val protocolOptions: ProtocolOptions = mock[ProtocolOptions]
  val metaData: Metadata = mock[Metadata]
  val keySpaceMetaData: KeyspaceMetadata = mock[KeyspaceMetadata]
  val resultSetFuture: ResultSetFuture = mock[ResultSetFuture]
  val columnMetadata: ColumnMetadata = mock[ColumnMetadata]

  "ConnectedRepository" should "provide repository session" in {
    // Given
    when(listenableFuture.get()).thenReturn(mock[Session])
    when(listenableFuture.isDone).thenReturn(true)
    when(cluster.connectAsync()).thenReturn(listenableFuture)

    // When
    Await.result(Await.result(ConnectedKeyspace("cassandra_connector"), 1 seconds).Session, 1 seconds)

    // Then
    verify(cluster).connectAsync()
  }

  "ConnectedRepository" should "run migrations" in {
    // Given
    when(listenableFuture.get()).thenReturn(mock[Session])
    when(listenableFuture.isDone).thenReturn(true)
    when(cluster.connectAsync()).thenReturn(listenableFuture)

    // When
    val connectedRepository: Future[ConnectedKeyspace] = ConnectedKeyspace( "cassandra_connector", Some("aGivenDirectoryWithDotCqlFiles"))


    // Then
    connectedRepository.isInstanceOf[Future[ConnectedKeyspace] ] should be(true)
  }

  "ConnectedRepository" should "close repository session" in {
    // Given
    when(cluster.closeAsync()).thenReturn(closeFuture)
    when(closeFuture.isDone).thenReturn(true)

    // When
    Await.result(Await.result(ConnectedKeyspace("cassandra_connector"), 1 second).close, 1 seconds)

    // Then
    verify(cluster).closeAsync()
  }


  "ConnectedRepository" should "map multiple entities" in {
    // Given
    val tableMetaData = mock[TableMetadata]
    setUpMapperMocks(tableMetaData)


    // When
    when(keySpaceMetaData.getTable("users")).thenReturn(tableMetaData)
    val keyspace = Await.result(ConnectedKeyspace("cassandra_connector"), 1 second)
    val userMapper: Mapper[TestUser] = Await.result(keyspace.materialise[TestUser], 1 second)

    // Then
    userMapper should not be null
    userMapper.getTableMetadata should equal(tableMetaData)


    //When
    when(keySpaceMetaData.getTable("address")).thenReturn(tableMetaData)
    val addressMapper: Mapper[TestAddress] = Await.result(keyspace.materialise[TestAddress], 1 second)


    // Then
    addressMapper should not be null
  }


  "ConnectedRepository" should "get Manager" in {
    // Given
    val tableMetaData = mock[TableMetadata]
    setUpMapperMocks(tableMetaData)


    // When
    when(keySpaceMetaData.getTable("users")).thenReturn(tableMetaData)
    val keyspace = Await.result(ConnectedKeyspace("cassandra_connector"), 1 second)


    // Then
    Await.result(keyspace.Manager, 1 second).getSession should equal(session)
  }



  private def setUpMapperMocks(tableMetaData: TableMetadata): Unit = {



    when(listenableFuture.get()).thenReturn(session)
    when(listenableFuture.isDone).thenReturn(true)
    when(cluster.connectAsync()).thenReturn(listenableFuture)
    when(session.getCluster).thenReturn(cluster)
    when(resultSetFuture.isDone).thenReturn(true)
    when(session.executeAsync(any(classOf[String]))).thenReturn(resultSetFuture)
    when(cluster.getConfiguration).thenReturn(configuration)
    when(configuration.getProtocolOptions).thenReturn(protocolOptions)
    when(protocolOptions.getProtocolVersion).thenReturn(ProtocolVersion.NEWEST_SUPPORTED)
    when(cluster.getMetadata).thenReturn(metaData)
    when(metaData.getKeyspace("cassandra_connector")).thenReturn(keySpaceMetaData)
    when(keySpaceMetaData.getTable("users")).thenReturn(tableMetaData)
    when(tableMetaData.getColumn(any())).thenReturn(columnMetadata)

  }
}
