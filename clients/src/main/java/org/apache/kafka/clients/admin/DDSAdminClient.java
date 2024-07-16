package org.apache.kafka.clients.admin;

import org.apache.kafka.clients.ApiVersions;
import org.apache.kafka.clients.ClientRequest;
import org.apache.kafka.clients.ClientResponse;
import org.apache.kafka.clients.ClientUtils;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.DefaultHostResolver;
import org.apache.kafka.clients.HostResolver;
import org.apache.kafka.clients.KafkaClient;
import org.apache.kafka.clients.LeastLoadedNode;
import org.apache.kafka.clients.MetadataRecoveryStrategy;
import org.apache.kafka.clients.NetworkClient;
import org.apache.kafka.clients.StaleMetadataException;
import org.apache.kafka.clients.admin.CreateTopicsResult.TopicMetadataAndConfig;
import org.apache.kafka.clients.admin.DeleteAclsResult.FilterResult;
import org.apache.kafka.clients.admin.DeleteAclsResult.FilterResults;
import org.apache.kafka.clients.admin.DescribeReplicaLogDirsResult.ReplicaLogDirInfo;
import org.apache.kafka.clients.admin.ListOffsetsResult.ListOffsetsResultInfo;
import org.apache.kafka.clients.admin.OffsetSpec.TimestampSpec;
import org.apache.kafka.clients.admin.internals.AbortTransactionHandler;
import org.apache.kafka.clients.admin.internals.AdminApiDriver;
import org.apache.kafka.clients.admin.internals.AdminApiFuture;
import org.apache.kafka.clients.admin.internals.AdminApiFuture.SimpleAdminApiFuture;
import org.apache.kafka.clients.admin.internals.AdminApiHandler;
import org.apache.kafka.clients.admin.internals.AdminBootstrapAddresses;
import org.apache.kafka.clients.admin.internals.AdminMetadataManager;
import org.apache.kafka.clients.admin.internals.AllBrokersStrategy;
import org.apache.kafka.clients.admin.internals.AlterConsumerGroupOffsetsHandler;
import org.apache.kafka.clients.admin.internals.CoordinatorKey;
import org.apache.kafka.clients.admin.internals.DeleteConsumerGroupOffsetsHandler;
import org.apache.kafka.clients.admin.internals.DeleteConsumerGroupsHandler;
import org.apache.kafka.clients.admin.internals.DeleteRecordsHandler;
import org.apache.kafka.clients.admin.internals.DescribeConsumerGroupsHandler;
import org.apache.kafka.clients.admin.internals.DescribeProducersHandler;
import org.apache.kafka.clients.admin.internals.DescribeTransactionsHandler;
import org.apache.kafka.clients.admin.internals.FenceProducersHandler;
import org.apache.kafka.clients.admin.internals.ListConsumerGroupOffsetsHandler;
import org.apache.kafka.clients.admin.internals.ListOffsetsHandler;
import org.apache.kafka.clients.admin.internals.ListTransactionsHandler;
import org.apache.kafka.clients.admin.internals.RemoveMembersFromConsumerGroupHandler;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.consumer.internals.ConsumerProtocol;
import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.ConsumerGroupState;
import org.apache.kafka.common.ElectionType;
import org.apache.kafka.common.GroupType;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.Metric;
import org.apache.kafka.common.MetricName;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicCollection;
import org.apache.kafka.common.TopicCollection.TopicIdCollection;
import org.apache.kafka.common.TopicCollection.TopicNameCollection;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.TopicPartitionInfo;
import org.apache.kafka.common.TopicPartitionReplica;
import org.apache.kafka.common.Uuid;
import org.apache.kafka.common.acl.AclBinding;
import org.apache.kafka.common.acl.AclBindingFilter;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.annotation.InterfaceStability;
import org.apache.kafka.common.config.ConfigException;
import org.apache.kafka.common.config.ConfigResource;
import org.apache.kafka.common.errors.ApiException;
import org.apache.kafka.common.errors.AuthenticationException;
import org.apache.kafka.common.errors.DisconnectException;
import org.apache.kafka.common.errors.InvalidRequestException;
import org.apache.kafka.common.errors.InvalidTopicException;
import org.apache.kafka.common.errors.KafkaStorageException;
import org.apache.kafka.common.errors.MismatchedEndpointTypeException;
import org.apache.kafka.common.errors.RetriableException;
import org.apache.kafka.common.errors.ThrottlingQuotaExceededException;
import org.apache.kafka.common.errors.TimeoutException;
import org.apache.kafka.common.errors.UnacceptableCredentialException;
import org.apache.kafka.common.errors.UnknownServerException;
import org.apache.kafka.common.errors.UnknownTopicIdException;
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException;
import org.apache.kafka.common.errors.UnsupportedEndpointTypeException;
import org.apache.kafka.common.errors.UnsupportedSaslMechanismException;
import org.apache.kafka.common.errors.UnsupportedVersionException;
import org.apache.kafka.common.internals.KafkaFutureImpl;
import org.apache.kafka.common.message.AddRaftVoterRequestData;
import org.apache.kafka.common.message.AlterPartitionReassignmentsRequestData;
import org.apache.kafka.common.message.AlterPartitionReassignmentsRequestData.ReassignableTopic;
import org.apache.kafka.common.message.AlterReplicaLogDirsRequestData;
import org.apache.kafka.common.message.AlterReplicaLogDirsRequestData.AlterReplicaLogDir;
import org.apache.kafka.common.message.AlterReplicaLogDirsRequestData.AlterReplicaLogDirTopic;
import org.apache.kafka.common.message.AlterReplicaLogDirsResponseData.AlterReplicaLogDirPartitionResult;
import org.apache.kafka.common.message.AlterReplicaLogDirsResponseData.AlterReplicaLogDirTopicResult;
import org.apache.kafka.common.message.AlterUserScramCredentialsRequestData;
import org.apache.kafka.common.message.ApiVersionsResponseData.FinalizedFeatureKey;
import org.apache.kafka.common.message.ApiVersionsResponseData.SupportedFeatureKey;
import org.apache.kafka.common.message.CreateAclsRequestData;
import org.apache.kafka.common.message.CreateAclsRequestData.AclCreation;
import org.apache.kafka.common.message.CreateAclsResponseData.AclCreationResult;
import org.apache.kafka.common.message.CreateDelegationTokenRequestData;
import org.apache.kafka.common.message.CreateDelegationTokenRequestData.CreatableRenewers;
import org.apache.kafka.common.message.CreateDelegationTokenResponseData;
import org.apache.kafka.common.message.CreatePartitionsRequestData;
import org.apache.kafka.common.message.CreatePartitionsRequestData.CreatePartitionsAssignment;
import org.apache.kafka.common.message.CreatePartitionsRequestData.CreatePartitionsTopic;
import org.apache.kafka.common.message.CreatePartitionsRequestData.CreatePartitionsTopicCollection;
import org.apache.kafka.common.message.CreatePartitionsResponseData.CreatePartitionsTopicResult;
import org.apache.kafka.common.message.CreateTopicsRequestData;
import org.apache.kafka.common.message.CreateTopicsRequestData.CreatableTopicCollection;
import org.apache.kafka.common.message.CreateTopicsResponseData.CreatableTopicConfigs;
import org.apache.kafka.common.message.CreateTopicsResponseData.CreatableTopicResult;
import org.apache.kafka.common.message.DeleteAclsRequestData;
import org.apache.kafka.common.message.DeleteAclsRequestData.DeleteAclsFilter;
import org.apache.kafka.common.message.DeleteAclsResponseData;
import org.apache.kafka.common.message.DeleteAclsResponseData.DeleteAclsFilterResult;
import org.apache.kafka.common.message.DeleteAclsResponseData.DeleteAclsMatchingAcl;
import org.apache.kafka.common.message.DeleteTopicsRequestData;
import org.apache.kafka.common.message.DeleteTopicsRequestData.DeleteTopicState;
import org.apache.kafka.common.message.DeleteTopicsResponseData.DeletableTopicResult;
import org.apache.kafka.common.message.DescribeClusterRequestData;
import org.apache.kafka.common.message.DescribeClusterResponseData;
import org.apache.kafka.common.message.DescribeConfigsRequestData;
import org.apache.kafka.common.message.DescribeConfigsResponseData;
import org.apache.kafka.common.message.DescribeLogDirsRequestData;
import org.apache.kafka.common.message.DescribeLogDirsRequestData.DescribableLogDirTopic;
import org.apache.kafka.common.message.DescribeLogDirsResponseData;
import org.apache.kafka.common.message.DescribeQuorumResponseData;
import org.apache.kafka.common.message.DescribeTopicPartitionsRequestData;
import org.apache.kafka.common.message.DescribeTopicPartitionsRequestData.TopicRequest;
import org.apache.kafka.common.message.DescribeTopicPartitionsResponseData;
import org.apache.kafka.common.message.DescribeTopicPartitionsResponseData.DescribeTopicPartitionsResponsePartition;
import org.apache.kafka.common.message.DescribeTopicPartitionsResponseData.DescribeTopicPartitionsResponseTopic;
import org.apache.kafka.common.message.DescribeUserScramCredentialsRequestData;
import org.apache.kafka.common.message.DescribeUserScramCredentialsRequestData.UserName;
import org.apache.kafka.common.message.DescribeUserScramCredentialsResponseData;
import org.apache.kafka.common.message.ExpireDelegationTokenRequestData;
import org.apache.kafka.common.message.GetTelemetrySubscriptionsRequestData;
import org.apache.kafka.common.message.LeaveGroupRequestData.MemberIdentity;
import org.apache.kafka.common.message.ListClientMetricsResourcesRequestData;
import org.apache.kafka.common.message.ListGroupsRequestData;
import org.apache.kafka.common.message.ListGroupsResponseData;
import org.apache.kafka.common.message.ListPartitionReassignmentsRequestData;
import org.apache.kafka.common.message.MetadataRequestData;
import org.apache.kafka.common.message.RemoveRaftVoterRequestData;
import org.apache.kafka.common.message.RenewDelegationTokenRequestData;
import org.apache.kafka.common.message.UnregisterBrokerRequestData;
import org.apache.kafka.common.message.UpdateFeaturesRequestData;
import org.apache.kafka.common.message.UpdateFeaturesResponseData.UpdatableFeatureResult;
import org.apache.kafka.common.metrics.KafkaMetricsContext;
import org.apache.kafka.common.metrics.MetricConfig;
import org.apache.kafka.common.metrics.Metrics;
import org.apache.kafka.common.metrics.MetricsContext;
import org.apache.kafka.common.metrics.MetricsReporter;
import org.apache.kafka.common.metrics.Sensor;
import org.apache.kafka.common.protocol.Errors;
import org.apache.kafka.common.quota.ClientQuotaAlteration;
import org.apache.kafka.common.quota.ClientQuotaEntity;
import org.apache.kafka.common.quota.ClientQuotaFilter;
import org.apache.kafka.common.requests.AbstractRequest;
import org.apache.kafka.common.requests.AbstractResponse;
import org.apache.kafka.common.requests.AddRaftVoterRequest;
import org.apache.kafka.common.requests.AddRaftVoterResponse;
import org.apache.kafka.common.requests.AlterClientQuotasRequest;
import org.apache.kafka.common.requests.AlterClientQuotasResponse;
import org.apache.kafka.common.requests.AlterConfigsRequest;
import org.apache.kafka.common.requests.AlterConfigsResponse;
import org.apache.kafka.common.requests.AlterPartitionReassignmentsRequest;
import org.apache.kafka.common.requests.AlterPartitionReassignmentsResponse;
import org.apache.kafka.common.requests.AlterReplicaLogDirsRequest;
import org.apache.kafka.common.requests.AlterReplicaLogDirsResponse;
import org.apache.kafka.common.requests.AlterUserScramCredentialsRequest;
import org.apache.kafka.common.requests.AlterUserScramCredentialsResponse;
import org.apache.kafka.common.requests.ApiError;
import org.apache.kafka.common.requests.ApiVersionsRequest;
import org.apache.kafka.common.requests.ApiVersionsResponse;
import org.apache.kafka.common.requests.CreateAclsRequest;
import org.apache.kafka.common.requests.CreateAclsResponse;
import org.apache.kafka.common.requests.CreateDelegationTokenRequest;
import org.apache.kafka.common.requests.CreateDelegationTokenResponse;
import org.apache.kafka.common.requests.CreatePartitionsRequest;
import org.apache.kafka.common.requests.CreatePartitionsResponse;
import org.apache.kafka.common.requests.CreateTopicsRequest;
import org.apache.kafka.common.requests.CreateTopicsResponse;
import org.apache.kafka.common.requests.DeleteAclsRequest;
import org.apache.kafka.common.requests.DeleteAclsResponse;
import org.apache.kafka.common.requests.DeleteTopicsRequest;
import org.apache.kafka.common.requests.DeleteTopicsResponse;
import org.apache.kafka.common.requests.DescribeAclsRequest;
import org.apache.kafka.common.requests.DescribeAclsResponse;
import org.apache.kafka.common.requests.DescribeClientQuotasRequest;
import org.apache.kafka.common.requests.DescribeClientQuotasResponse;
import org.apache.kafka.common.requests.DescribeClusterRequest;
import org.apache.kafka.common.requests.DescribeClusterResponse;
import org.apache.kafka.common.requests.DescribeConfigsRequest;
import org.apache.kafka.common.requests.DescribeConfigsResponse;
import org.apache.kafka.common.requests.DescribeDelegationTokenRequest;
import org.apache.kafka.common.requests.DescribeDelegationTokenResponse;
import org.apache.kafka.common.requests.DescribeLogDirsRequest;
import org.apache.kafka.common.requests.DescribeLogDirsResponse;
import org.apache.kafka.common.requests.DescribeQuorumRequest;
import org.apache.kafka.common.requests.DescribeQuorumRequest.Builder;
import org.apache.kafka.common.requests.DescribeQuorumResponse;
import org.apache.kafka.common.requests.DescribeTopicPartitionsRequest;
import org.apache.kafka.common.requests.DescribeTopicPartitionsResponse;
import org.apache.kafka.common.requests.DescribeUserScramCredentialsRequest;
import org.apache.kafka.common.requests.DescribeUserScramCredentialsResponse;
import org.apache.kafka.common.requests.ElectLeadersRequest;
import org.apache.kafka.common.requests.ElectLeadersResponse;
import org.apache.kafka.common.requests.ExpireDelegationTokenRequest;
import org.apache.kafka.common.requests.ExpireDelegationTokenResponse;
import org.apache.kafka.common.requests.GetTelemetrySubscriptionsRequest;
import org.apache.kafka.common.requests.GetTelemetrySubscriptionsResponse;
import org.apache.kafka.common.requests.IncrementalAlterConfigsRequest;
import org.apache.kafka.common.requests.IncrementalAlterConfigsResponse;
import org.apache.kafka.common.requests.JoinGroupRequest;
import org.apache.kafka.common.requests.ListClientMetricsResourcesRequest;
import org.apache.kafka.common.requests.ListClientMetricsResourcesResponse;
import org.apache.kafka.common.requests.ListGroupsRequest;
import org.apache.kafka.common.requests.ListGroupsResponse;
import org.apache.kafka.common.requests.ListOffsetsRequest;
import org.apache.kafka.common.requests.ListPartitionReassignmentsRequest;
import org.apache.kafka.common.requests.ListPartitionReassignmentsResponse;
import org.apache.kafka.common.requests.MetadataRequest;
import org.apache.kafka.common.requests.MetadataResponse;
import org.apache.kafka.common.requests.RemoveRaftVoterRequest;
import org.apache.kafka.common.requests.RenewDelegationTokenRequest;
import org.apache.kafka.common.requests.RenewDelegationTokenResponse;
import org.apache.kafka.common.requests.UnregisterBrokerRequest;
import org.apache.kafka.common.requests.UnregisterBrokerResponse;
import org.apache.kafka.common.requests.UpdateFeaturesRequest;
import org.apache.kafka.common.requests.UpdateFeaturesResponse;
import org.apache.kafka.common.security.auth.KafkaPrincipal;
import org.apache.kafka.common.security.scram.internals.ScramFormatter;
import org.apache.kafka.common.security.token.delegation.DelegationToken;
import org.apache.kafka.common.security.token.delegation.TokenInformation;
import org.apache.kafka.common.utils.AppInfoParser;
import org.apache.kafka.common.utils.ExponentialBackoff;
import org.apache.kafka.common.utils.KafkaThread;
import org.apache.kafka.common.utils.LogContext;
import org.apache.kafka.common.utils.ProducerIdAndEpoch;
import org.apache.kafka.common.utils.Time;
import org.apache.kafka.common.utils.Utils;

// import org.slf4j.Logger;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.kafka.common.internals.Topic.CLUSTER_METADATA_TOPIC_NAME;
import static org.apache.kafka.common.internals.Topic.CLUSTER_METADATA_TOPIC_PARTITION;
import static org.apache.kafka.common.message.AlterPartitionReassignmentsRequestData.ReassignablePartition;
import static org.apache.kafka.common.message.AlterPartitionReassignmentsResponseData.ReassignablePartitionResponse;
import static org.apache.kafka.common.message.AlterPartitionReassignmentsResponseData.ReassignableTopicResponse;
import static org.apache.kafka.common.message.ListPartitionReassignmentsRequestData.ListPartitionReassignmentsTopics;
import static org.apache.kafka.common.message.ListPartitionReassignmentsResponseData.OngoingPartitionReassignment;
import static org.apache.kafka.common.message.ListPartitionReassignmentsResponseData.OngoingTopicReassignment;
import static org.apache.kafka.common.requests.MetadataRequest.convertToMetadataRequestTopic;
import static org.apache.kafka.common.requests.MetadataRequest.convertTopicIdsToMetadataRequestTopic;
import static org.apache.kafka.common.utils.Utils.closeQuietly;

public class DDSAdminClient extends AdminClient{

	private long DDSMetadataClient_ptr;
	
	private static final String NOT_IMPLEMENTED_MESSAGE = "Not Implemented for Cascade DDS";
	static{
		System.loadLibrary("admin_DDS"); 
	}

	public DDSAdminClient(long input){
		this.DDSMetadataClient_ptr = input;
	}

	public static native long createInternal_native();

	public static Admin createInternal(){return new DDSAdminClient(createInternal_native());}

	public native void createTopics_native(long pointer, Collection<NewTopic> newTopics);

	@Override	
	public CreateTopicsResult createTopics(Collection<NewTopic> newTopics){
		this.createTopics_native(this.DDSMetadataClient_ptr, newTopics);
		Map<String, KafkaFuture<TopicMetadataAndConfig>> emptyMap = new HashMap<>();

		return new CreateTopicsResult(emptyMap);
	}
	

	@Override
	public native void close(Duration timeout);

    @Override
    public CreateTopicsResult createTopics(Collection<NewTopic> newTopics, CreateTopicsOptions options) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
    }

    @Override
    public DeleteTopicsResult deleteTopics(TopicCollection topics, DeleteTopicsOptions options) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
    }

    @Override
    public ListTopicsResult listTopics(ListTopicsOptions options) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
    }

    @Override
    public DescribeTopicsResult describeTopics(TopicCollection topics, DescribeTopicsOptions options) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
    }

    @Override
    public DescribeClusterResult describeCluster(DescribeClusterOptions options) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
    }

    @Override
    public DescribeAclsResult describeAcls(AclBindingFilter filter, DescribeAclsOptions options) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
    }

    @Override
    public CreateAclsResult createAcls(Collection<AclBinding> acls, CreateAclsOptions options) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
    }

    @Override
    public DeleteAclsResult deleteAcls(Collection<AclBindingFilter> filters, DeleteAclsOptions options) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
    }

    @Override
    public DescribeConfigsResult describeConfigs(Collection<ConfigResource> resources, DescribeConfigsOptions options) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
    }

   @Override
   @Deprecated
    public AlterConfigsResult alterConfigs(Map<ConfigResource, Config> configs, AlterConfigsOptions options) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
    }

    @Override
    public AlterConfigsResult incrementalAlterConfigs(Map<ConfigResource, Collection<AlterConfigOp>> configs, AlterConfigsOptions options) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
    }

    @Override
    public AlterReplicaLogDirsResult alterReplicaLogDirs(Map<TopicPartitionReplica, String> replicaAssignment, AlterReplicaLogDirsOptions options) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
    }

    @Override
    public DescribeLogDirsResult describeLogDirs(Collection<Integer> brokers, DescribeLogDirsOptions options) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
    }

    @Override
    public DescribeReplicaLogDirsResult describeReplicaLogDirs(Collection<TopicPartitionReplica> replicas, DescribeReplicaLogDirsOptions options) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
    }

    @Override
    public CreatePartitionsResult createPartitions(Map<String, NewPartitions> newPartitions, CreatePartitionsOptions options) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
    }

    @Override
    public DeleteRecordsResult deleteRecords(Map<TopicPartition, RecordsToDelete> recordsToDelete, DeleteRecordsOptions options) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
    }

    @Override
    public CreateDelegationTokenResult createDelegationToken(CreateDelegationTokenOptions options) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
    }

    @Override
    public RenewDelegationTokenResult renewDelegationToken(byte[] hmac, RenewDelegationTokenOptions options) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
    }

    @Override
    public ExpireDelegationTokenResult expireDelegationToken(byte[] hmac, ExpireDelegationTokenOptions options) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
    }

    @Override
    public DescribeDelegationTokenResult describeDelegationToken(DescribeDelegationTokenOptions options) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
    }

    @Override
    public DescribeConsumerGroupsResult describeConsumerGroups(Collection<String> groupIds, DescribeConsumerGroupsOptions options) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
    }

    @Override
    public ListConsumerGroupsResult listConsumerGroups(ListConsumerGroupsOptions options) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
    }

    @Override
    public ListConsumerGroupOffsetsResult listConsumerGroupOffsets(Map<String, ListConsumerGroupOffsetsSpec> groupSpecs, ListConsumerGroupOffsetsOptions options) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
    }

    @Override
    public DeleteConsumerGroupsResult deleteConsumerGroups(Collection<String> groupIds, DeleteConsumerGroupsOptions options) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
    }

    @Override
    public DeleteConsumerGroupOffsetsResult deleteConsumerGroupOffsets(String groupId, Set<TopicPartition> partitions, DeleteConsumerGroupOffsetsOptions options) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
    }

    @Override
    public ElectLeadersResult electLeaders(ElectionType electionType, Set<TopicPartition> partitions, ElectLeadersOptions options) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
    }

    @Override
    public AlterPartitionReassignmentsResult alterPartitionReassignments(Map<TopicPartition, Optional<NewPartitionReassignment>> reassignments, AlterPartitionReassignmentsOptions options) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
    }

    @Override
    public ListPartitionReassignmentsResult listPartitionReassignments(Optional<Set<TopicPartition>> partitions, ListPartitionReassignmentsOptions options) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
    }

    @Override
    public RemoveMembersFromConsumerGroupResult removeMembersFromConsumerGroup(String groupId, RemoveMembersFromConsumerGroupOptions options) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
    }

    @Override
    public AlterConsumerGroupOffsetsResult alterConsumerGroupOffsets(String groupId, Map<TopicPartition, OffsetAndMetadata> offsets, AlterConsumerGroupOffsetsOptions options) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
    }

    @Override
    public ListOffsetsResult listOffsets(Map<TopicPartition, OffsetSpec> topicPartitionOffsets, ListOffsetsOptions options) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
    }

    @Override
    public DescribeClientQuotasResult describeClientQuotas(ClientQuotaFilter filter, DescribeClientQuotasOptions options) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
    }

    @Override
    public AlterClientQuotasResult alterClientQuotas(Collection<ClientQuotaAlteration> entries, AlterClientQuotasOptions options) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
    }

    @Override
    public DescribeUserScramCredentialsResult describeUserScramCredentials(List<String> users, DescribeUserScramCredentialsOptions options) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
    }

    @Override
    public AlterUserScramCredentialsResult alterUserScramCredentials(List<UserScramCredentialAlteration> alterations, AlterUserScramCredentialsOptions options) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
    }

    @Override
    public DescribeFeaturesResult describeFeatures(DescribeFeaturesOptions options) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
    }

    @Override
    public UpdateFeaturesResult updateFeatures(Map<String, FeatureUpdate> featureUpdates, UpdateFeaturesOptions options) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
    }

    @Override
    public DescribeMetadataQuorumResult describeMetadataQuorum(DescribeMetadataQuorumOptions options) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
    }

    @Override
    public UnregisterBrokerResult unregisterBroker(int brokerId, UnregisterBrokerOptions options) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
    }

    @Override
    public DescribeProducersResult describeProducers(Collection<TopicPartition> partitions, DescribeProducersOptions options) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
    }

    @Override
    public DescribeTransactionsResult describeTransactions(Collection<String> transactionalIds, DescribeTransactionsOptions options) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
    }

    @Override
    public AbortTransactionResult abortTransaction(AbortTransactionSpec spec, AbortTransactionOptions options) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
    }

    @Override
    public ListTransactionsResult listTransactions(ListTransactionsOptions options) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
    }

    @Override
    public FenceProducersResult fenceProducers(Collection<String> transactionalIds, FenceProducersOptions options) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
    }

    @Override
    public ListClientMetricsResourcesResult listClientMetricsResources(ListClientMetricsResourcesOptions options) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
    }

    @Override
    public Uuid clientInstanceId(Duration timeout) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
    }

    @Override
    public AddRaftVoterResult addRaftVoter(int voterId, Uuid voterDirectoryId, Set<RaftVoterEndpoint> endpoints, AddRaftVoterOptions options) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
    }

    @Override
    public RemoveRaftVoterResult removeRaftVoter(int voterId, Uuid voterDirectoryId, RemoveRaftVoterOptions options) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
    }

    @Override
    public Map<MetricName, ? extends Metric> metrics() {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
    }


}
