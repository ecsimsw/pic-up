# global exchange
rabbitmqadmin -u ${USERNAME} -p ${PASSWORD} declare exchange \
      name=global_exchange \
      type=direct

# global dlq exchange
rabbitmqadmin -u ${USERNAME} -p ${PASSWORD} declare exchange \
      name=global_dlq_exchange \
      type=direct

# sign_up_queue
rabbitmqadmin -u ${USERNAME} -p ${PASSWORD} declare queue \
      name=sign_up_queue \
      durable=true \
      arguments='{"x-dead-letter-exchange":"global_dlq_exchange","x-dead-letter-routing-key":"sign_up_dead_letter"}'

# sign_up_queue binding
rabbitmqadmin -u ${USERNAME} -p ${PASSWORD} declare binding \
      source=global_exchange \
      destination=sign_up_queue \
      routing_key=sign_up

# sign_up_dlq
rabbitmqadmin -u ${USERNAME} -p ${PASSWORD} declare queue \
      name=sign_up_dlq \
      durable=true

# sign_up_dlq binding
rabbitmqadmin -u ${USERNAME} -p ${PASSWORD} declare binding \
      source=global_dlq_exchange \
      destination=sign_up_dlq \
      routing_key=sign_up_dead_letter

# user_deletion_queue
rabbitmqadmin -u ${USERNAME} -p ${PASSWORD} declare queue \
      name=user_delete_queue \
      durable=true \
      arguments='{"x-dead-letter-exchange":"global_dlq_exchange","x-dead-letter-routing-key":"user_delete_dead_letter"}'

# user_deletion_queue binding
rabbitmqadmin -u ${USERNAME} -p ${PASSWORD} declare binding \
      source=global_exchange \
      destination=user_delete_queue \
      routing_key=user_delete

# user_deletion_dlq
rabbitmqadmin -u ${USERNAME} -p ${PASSWORD} declare queue \
      name=user_delete_dlq \
      durable=true

# user_deletion_dlq binding
rabbitmqadmin -u ${USERNAME} -p ${PASSWORD} declare binding \
      source=global_dlq_exchange \
      destination=user_delete_dlq \
      routing_key=user_delete_dead_letter