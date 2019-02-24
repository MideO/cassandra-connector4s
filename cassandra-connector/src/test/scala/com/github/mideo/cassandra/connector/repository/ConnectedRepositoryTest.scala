package com.github.mideo.cassandra.connector.repository

import java.util.UUID

import com.datastax.driver.core._
import com.datastax.driver.mapping.Mapper
import com.datastax.driver.mapping.annotations.{Column, PartitionKey, Table}
import com.github.mideo.cassandra.connector.CassandraConnectorTest
import com.google.common.util.concurrent.ListenableFuture
import org.mockito.Matchers.any
import org.mockito.Mockito._

import scala.concurrent.Await
import scala.concurrent.duration._


class ConnectedRepositoryTest extends CassandraConnectorTest {
  val cluster: Cluster = mock[Cluster]
  val listenableFuture: ListenableFuture[Session] = mock[ListenableFuture[Session]]
  val closeFuture: CloseFuture = mock[CloseFuture]


  "ConnectedRepository" should "provide repository session" in {
    // Given
    when(listenableFuture.get()).thenReturn(mock[Session])
    when(listenableFuture.isDone).thenReturn(true)
    when(cluster.connectAsync()).thenReturn(listenableFuture)

    // When
    Await.result(ConnectedRepository(() => cluster, "Keyspace").session.connectAsync, 1 seconds)

    // Then
    verify(cluster).connectAsync()
  }

  "ConnectedRepository" should "close repository session" in {
    // Given
    when(cluster.closeAsync()).thenReturn(closeFuture)
    when(closeFuture.isDone).thenReturn(true)

    // When
    Await.result(ConnectedRepository(() => cluster, "keyspace").session.close, 1 seconds)

    // Then
    verify(cluster).closeAsync()
  }


  "ConnectedRepository" should "map multiple entities" in {
    // Given
    val tableMetaData = mock[TableMetadata]
    setUpMapperMocks(tableMetaData)

    @Table(keyspace = "Keyspace", name = "table")
    case class User(@PartitionKey @Column(name = "user_id") userId: UUID, @Column(name = "name") name: String)

    @Table(keyspace = "Keyspace", name = "table")
    case class Address(@PartitionKey @Column(name = "address_id") addressId: UUID, @Column(name = "name") name: String)

    // When
    val manager = ConnectedRepository(() => cluster, "Keyspace").repositoryMapper
    val userMapper: Mapper[User] = Await.result(manager.materialise(classOf[User]), 1 second)

    // Then
    userMapper should not be null
    userMapper.getTableMetadata should equal(tableMetaData)


    //When
    val addressMapper: Mapper[Address] = Await.result(manager.materialise(classOf[Address]), 1 second)


    // Then
    addressMapper should not be null
  }


  private def setUpMapperMocks(tableMetaData: TableMetadata): Unit = {
    val session = mock[Session]
    val configuration = mock[Configuration]
    val protocolOptions = mock[ProtocolOptions]
    val metaData = mock[Metadata]
    val keySpaceMetaData = mock[KeyspaceMetadata]

    val columnMetadata = mock[ColumnMetadata]

    when(listenableFuture.get()).thenReturn(session)
    when(listenableFuture.isDone).thenReturn(true)
    when(cluster.connectAsync()).thenReturn(listenableFuture)
    when(session.getCluster).thenReturn(cluster)
    when(cluster.getConfiguration).thenReturn(configuration)
    when(configuration.getProtocolOptions).thenReturn(protocolOptions)
    when(protocolOptions.getProtocolVersion).thenReturn(ProtocolVersion.NEWEST_SUPPORTED)
    when(cluster.getMetadata).thenReturn(metaData)
    when(metaData.getKeyspace("Keyspace")).thenReturn(keySpaceMetaData)
    when(keySpaceMetaData.getTable("table")).thenReturn(tableMetaData)
    when(tableMetaData.getColumn(any())).thenReturn(columnMetadata)

  }
}
