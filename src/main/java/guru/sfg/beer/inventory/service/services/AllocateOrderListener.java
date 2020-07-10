package guru.sfg.beer.inventory.service.services;

import guru.sfg.beer.inventory.service.config.JmsConfig;
import guru.sfg.brewery.model.BeerOrderDto;
import guru.sfg.brewery.model.events.AllocateOrderRequest;
import guru.sfg.brewery.model.events.AllocateOrderResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AllocateOrderListener {

    private final AllocationService allocationService;
    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = JmsConfig.ALLOCATE_ORDER_QUEUE)
    public void listen(AllocateOrderRequest request) {
        BeerOrderDto beerOrderDto = request.getBeerOrderDto();
        AllocateOrderResult.AllocateOrderResultBuilder resultBuilder = AllocateOrderResult.builder();

        resultBuilder.beerOrderDto(beerOrderDto);

        try{
            Boolean allocationResult = allocationService.allocateOrder(request.getBeerOrderDto());

            if (allocationResult){
                resultBuilder.pendingInventory(false);
            } else {
                resultBuilder.pendingInventory(true);
            }
        } catch (Exception e){
            log.error("Allocation failed for Order Id:" + request.getBeerOrderDto().getId());
            resultBuilder.allocationError(true);
        }

        jmsTemplate.convertAndSend(JmsConfig.ALLOCATE_ORDER_RESPONSE_QUEUE, resultBuilder.build());
    }

}
